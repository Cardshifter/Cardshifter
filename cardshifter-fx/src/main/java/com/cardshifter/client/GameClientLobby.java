package com.cardshifter.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteRequest;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.incoming.ServerQueryMessage.Request;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.api.outgoing.UserStatusMessage.Status;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameClientLobby implements Initializable {
	
	@FXML private ListView<String> usersOnline;
	@FXML private ListView<String> chatMessages;
	@FXML private TextField messageBox;
	@FXML private Button sendMessageButton;
	@FXML private AnchorPane inviteButton;
	@FXML private AnchorPane inviteWindow;
	
	private final ObjectMapper mapper = new ObjectMapper();
	private final Set<GameClientController> gamesRunning = new HashSet<>();
	private Socket socket;	
	private InputStream in;
	private OutputStream out;
	private String ipAddress;
	private int port;
	private String userName;
	
	private Thread listenThread;
	private final Map<String, Integer> usersOnlineList = new HashMap<>();
	private String userForGameInvite;
	private InviteRequest currentGameRequest;
	
	public void acceptConnectionSettings(String ipAddress, int port, String userName) {
		// this is passed into this object after it is automatically created by the FXML document
		this.ipAddress = ipAddress;
		this.port = port;
		this.userName = userName;
	}
	public boolean connectToLobby() {
		// this is called on the object before the scene is displayed
		try {
			this.socket = new Socket(this.ipAddress, this.port);
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();
			mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			this.listenThread = new Thread(this::listen);
			this.listenThread.start();
		} catch (IOException ex) {
			System.out.println("Connection Failed");
			return false;
		}
		
		this.sendLoginMessage();
		this.sendServerQueryMessage();
		
		this.usersOnline.setOnMouseClicked(this::selectUserForGameInvite);
		this.inviteButton.setOnMouseClicked(this::startGameWithUser);
		
		return true;
	}
	
	private void listen() {
		while (true) {
			try {
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				while (values.hasNextValue()) {
					Message message = values.next();
					//This is where all the magic happens for message handling
					Platform.runLater(() -> this.processMessageFromServer(message));
				}
			} catch (SocketException e) {
				System.out.println("Error receiving message: " + e.getMessage());
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void send(Message message) {
		try {
			System.out.println("Sending: " + this.mapper.writeValueAsString(message));
			this.mapper.writeValue(out, message);
		} catch (IOException e) {
			System.out.println("Error sending message: " + message);
			throw new RuntimeException(e);
		}
	}
	
	private void sendLoginMessage() {
		LoginMessage loginMessage = new LoginMessage(this.userName);
		this.send(loginMessage);
	}
	
	private void sendServerQueryMessage() {
		ServerQueryMessage initialMessage = new ServerQueryMessage(Request.USERS);
		this.send(initialMessage);
	}
	
	private void processMessageFromServer(Message message) {	
		//this is for diagnostics so I can copy paste the messages to know their format
		System.out.println(message.toString());
		
		for (GameClientController gameController : this.gamesRunning) {
			gameController.processMessageFromServer(message);
		}
		
		if (message instanceof NewGameMessage) {
			this.startNewGame((NewGameMessage)message);
		} else if (message instanceof UserStatusMessage) {
			this.processUserStatusMessage((UserStatusMessage)message);
		} else if (message instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) message;
			this.chatOutput(msg.getFrom() + ": " + msg.getMessage());
		} else if (message instanceof ServerErrorMessage) {
			ServerErrorMessage msg = (ServerErrorMessage) message;
			this.chatOutput("SERVER ERROR: " + msg.getMessage());
		} else if (message instanceof InviteRequest) {
			this.currentGameRequest = (InviteRequest)message;
			this.createInviteWindow((InviteRequest)message);
		}
	}
	
	private void startNewGame(NewGameMessage message) {
		if (!gamesRunning.isEmpty()) {
			this.chatOutput("You already have a running game. Unable to start a new one.");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientDocument.fxml"));
			Parent root = (Parent)loader.load();
			GameClientController controller = loader.<GameClientController>getController();
			this.gamesRunning.add(controller);
			controller.acceptConnectionSettings(message, this::send);
			
			Scene scene = new Scene(root);
			Stage gameStage = new Stage();
			gameStage.setScene(scene);
			gameStage.setOnCloseRequest(windowEvent -> this.closeController(controller));
			gameStage.show();
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private void processUserStatusMessage(UserStatusMessage message) {
		if (message.getStatus() == Status.ONLINE) {
			this.usersOnlineList.put(message.getName(), message.getUserId());
		} else if (message.getStatus() == Status.OFFLINE) {
			this.usersOnlineList.remove(message.getName());
		}
		
		this.usersOnline.getItems().clear();
		this.usersOnline.getItems().addAll(this.usersOnlineList.keySet());
	}
	
	
	private void chatOutput(String string) {
		Platform.runLater(() -> this.chatMessages.getItems().add(string));
	}

	private void createInviteWindow(InviteRequest message) {
		this.inviteWindow.setVisible(true);
		this.inviteWindow.getChildren().add(new InviteWindow(message, this).getRootPane());
	}
	
	public void acceptGameRequest(MouseEvent event) {
		this.send(new InviteResponse(this.currentGameRequest.getId(), true));
		this.closeInviteWindow();
	}
	
	public void declineGameRequest(MouseEvent event) {
		this.send(new InviteResponse(this.currentGameRequest.getId(), false));
		this.closeInviteWindow();
	}
	
	private void closeInviteWindow() {
		this.inviteWindow.getChildren().clear();
		this.inviteWindow.setVisible(false);
	}

	private void selectUserForGameInvite(MouseEvent event) {
		String selected = this.usersOnline.getSelectionModel().getSelectedItem();
		if (selected != null) {
			this.userForGameInvite = selected.toString();
		} else {
			this.usersOnline.getItems().clear();
			this.sendServerQueryMessage();
		}
	}
	
	private void startGameWithUser(MouseEvent event) {
		if (this.userForGameInvite != null) {
			int userIdToInvite = this.usersOnlineList.get(this.userForGameInvite);
			StartGameRequest startGameRequest = new StartGameRequest(userIdToInvite, CardshifterConstants.VANILLA);
			this.sendInvite(startGameRequest);
		}
	}
	
	private void sendInvite(StartGameRequest startGameRequest) {
		if (!gamesRunning.isEmpty()) {
			this.chatOutput("You already have a running game. Unable to start a new one.");
			return;
		}
		
		this.send(startGameRequest);
	}
	
	private void closeController(GameClientController controller) {
		this.gamesRunning.remove(controller);
		controller.closeGame();
	}
	
	private void stopThreads() {
		this.listenThread.interrupt();
	}
	
	private void breakConnection() {
		try {
			this.in.close();
			this.out.close();
		} catch (Exception e) {
			System.out.println("Failed to break connection");
		}
	}
	
	public void closeLobby() {
		this.stopThreads();
		this.breakConnection();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.sendMessageButton.setOnAction(e -> this.sendMessage());
		this.messageBox.setOnAction(e -> this.sendMessage());
	}
	
	private void sendMessage() {
		String message = this.messageBox.getText();
		this.send(new ChatMessage(1, "unused", message));
		this.messageBox.clear();
	}
	
}
