package com.cardshifter.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.zomis.cardshifter.ecs.config.DeckConfig;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteRequest;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.incoming.ServerQueryMessage.Request;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.AvailableModsMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.api.outgoing.UserStatusMessage.Status;
import com.cardshifter.client.buttons.GameTypeButton;
import com.cardshifter.client.buttons.GenericButton;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameClientLobby implements Initializable {
	
	@FXML private AnchorPane rootPane;
	@FXML private ListView<String> usersOnline;
	@FXML private ListView<String> chatMessages;
	@FXML private TextField messageBox;
	@FXML private Button sendMessageButton;
	@FXML private AnchorPane inviteButton;
	@FXML private AnchorPane inviteWindow;
	@FXML private HBox gameTypeBox;
	@FXML private AnchorPane deckBuilderButton;
	
	private final ObjectMapper mapper = CardshifterIO.mapper();
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
	private final List<String> gameTypes = new ArrayList<>();
	private String selectedGameType;
	private PlayerConfigMessage currentPlayerConfig;
	private DeckBuilderWindow openDeckBuilderWindow;
	
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
		this.deckBuilderButton.setOnMouseClicked(this::openDeckBuilderWindowWithoutGame);
		
		return true;
	}
	
	private void listen() {
		while (true) {
			if (socket.isClosed()) {
				chatOutput("Connection Closed");
				break;
			}
			try {
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				while (values.hasNextValue()) {
					Message message = values.next();
					//This is where all the magic happens for message handling
					Platform.runLater(() -> this.processMessageFromServer(message));
				}
			} catch (SocketException e) {
				this.chatOutput("Error receiving message: " + e.getMessage());
				return;
			} catch (IOException e) {
				this.chatOutput("Lost connection to server");
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
		} else if (message instanceof PlayerConfigMessage) {
			PlayerConfigMessage msg = (PlayerConfigMessage) message;
			this.showConfigDialog(msg);
		} else if (message instanceof InviteRequest) {
			this.currentGameRequest = (InviteRequest)message;
			this.createInviteWindow((InviteRequest)message);
		} else if (message instanceof AvailableModsMessage) {
			this.gameTypes.addAll(Arrays.asList(((AvailableModsMessage)message).getMods()));
			this.createGameTypeButtons();
		}
	}
	
	private void showConfigDialog(PlayerConfigMessage configMessage) {
		this.currentPlayerConfig = configMessage;
		
		Map<String, Object> configs = configMessage.getConfigs();
		
		for (Entry<String, Object> entry : configs.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof DeckConfig) {
				DeckConfig deckConfig = (DeckConfig) value;
				this.showDeckBuilderWindow(deckConfig, true);
			}
		}		
	}
	
	public void sendDeckAndPlayerConfigToServer(DeckConfig deckConfig) {
		Map<String, Object> configs = this.currentPlayerConfig.getConfigs();
		
		for (Entry<String, Object> entry : configs.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof DeckConfig) {
				DeckConfig config = (DeckConfig) value;
				deckConfig.getChosen().forEach((id, count) -> config.setChosen(id, count));
			}
		}
		
		this.send(new PlayerConfigMessage(this.currentPlayerConfig.getGameId(), configs));
	}
	
	private void openDeckBuilderWindowWithoutGame(MouseEvent event) {
		if (this.currentPlayerConfig != null) {
			Map<String, Object> configs = this.currentPlayerConfig.getConfigs();
		
			for (Entry<String, Object> entry : configs.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof DeckConfig) {
					DeckConfig deckConfig = (DeckConfig) value;
					this.showDeckBuilderWindow(deckConfig, false);
				}
			}		
		} else {
			//get the player config from the server?
		}
	}
	
	private void showDeckBuilderWindow(DeckConfig deckConfig, boolean startingGame) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("DeckBuilderDocument.fxml"));
			Parent root = (Parent)loader.load();
			DeckBuilderWindow controller = loader.<DeckBuilderWindow>getController();
			
			controller.acceptDeckConfig(deckConfig, conf -> this.sendDeckAndPlayerConfigToServer(conf));
			controller.configureWindow();
			
			this.openDeckBuilderWindow = controller;
			
			if (!startingGame) {
				controller.disableGameStart();
			}
			
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setOnCloseRequest(windowEvent -> this.closeDeckBuilderWindow());
			stage.show();
		}
        catch (Exception e) {
            throw new RuntimeException(e);
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
			chatOutput(message.getName() + " is now offline.");
		}
		
		this.usersOnline.getItems().clear();
		this.usersOnline.getItems().addAll(this.usersOnlineList.keySet());
	}
	
	
	private void chatOutput(String string) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
		String time = "[" + formatter.format(Instant.now()) + "] ";
		Platform.runLater(() -> this.chatMessages.getItems().add(time + string));
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
			this.userForGameInvite = selected;
		} else {
			this.usersOnline.getItems().clear();
			this.sendServerQueryMessage();
		}
	}
	
	private void startGameWithUser(MouseEvent event) {
		if (this.userForGameInvite != null) {
			if (this.selectedGameType != null) {
				int userIdToInvite = this.usersOnlineList.get(this.userForGameInvite);
				StartGameRequest startGameRequest = new StartGameRequest(userIdToInvite, this.selectedGameType);
				this.sendInvite(startGameRequest);
				this.chatOutput("Invite sent to " + this.userForGameInvite);
			} else {
				this.chatOutput("No Game Type selected");
			}
		} else {
			this.chatOutput("No Opponent selected");
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
		controller.closeWindow();
	}
	
	private void closeDeckBuilderWindow() {
		//this is a workaround to allow the player to "decline" an invite once the deck builder is open
		if(this.gamesRunning.size() == 1) {
			for (GameClientController controller : this.gamesRunning) {
				this.closeController(controller);
				this.openDeckBuilderWindow = null;
			}
		}
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
		
		for (GameClientController game : this.gamesRunning) {
			game.closeWindow();
		}
		
		if (this.openDeckBuilderWindow != null) {
			this.openDeckBuilderWindow.closeWindow();
		}
	}
	
	private void createGameTypeButtons() {
		for (String string : this.gameTypes) {
			GameTypeButton button = new GameTypeButton(this.gameTypeBox.getPrefWidth() / this.gameTypes.size(), this.gameTypeBox.getPrefHeight(), string, this);
			this.gameTypeBox.getChildren().add(button);
		}
	}
	
	public void clearGameTypeButtons() {
		for (Object button : this.gameTypeBox.getChildren()) {
			((GenericButton)button).unHighlightButton();
		}
	}
	
	public void setGameType(String string) {
		this.selectedGameType = string;
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
