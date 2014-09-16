package com.cardshifter.client;

import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.RequestTargetsMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.CardInfoMessage;
import com.cardshifter.server.outgoing.NewGameMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.cardshifter.server.outgoing.WaitMessage;
import com.cardshifter.server.outgoing.WelcomeMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

public class GameClientController {
	
	@FXML
	Label serverMessage;
	@FXML
	AnchorPane rootPane;
	@FXML
	ListView serverMessages;
	
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private final ObjectMapper mapper = new ObjectMapper();
	
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final List<UseableActionMessage> actions = Collections.synchronizedList(new ArrayList<>());
	private final List<CardInfoMessage> cards = Collections.synchronizedList(new ArrayList<>());
	private final List<Message> genericMessages = Collections.synchronizedList(new ArrayList<>());
	private final List<UseableActionMessage> actionsForServer = Collections.synchronizedList(new ArrayList<>());
	
	private int gameId;
	private String ipAddress;
	private int port;
	
	/////////INITIALIZATION///////////////
	//this is passed into the object after it is created by the FXML document
	public void acceptIPAndPort(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}
	//this is called on the object from the Game launcher before the scene is displayed
	public void connectToGame() {
		try {
			this.socket = new Socket(this.ipAddress, this.port);
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();
			mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			new Thread(this::listen).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			new Thread(this::play).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//this method only runs once at the start
	public void play() {
		String name = "Player" + new Random().nextInt(100);
		this.send(new LoginMessage(name));
		
		try {
			WelcomeMessage response = (WelcomeMessage) messages.take();
			if (!response.isOK()) {
				return;
			}
			
			//display the welcome message on the screen
			Platform.runLater(() -> serverMessage.setText(response.getMessage()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.send(new StartGameRequest());
		
		try {
			Message message = messages.take();
			if (message instanceof WaitMessage) {	
				
				//display the wait message on the screen
				Platform.runLater(() -> serverMessage.setText(((WaitMessage)message).getMessage()));

				NewGameMessage game = (NewGameMessage) messages.take();
				this.playLoop();
			}
			else {
				this.playLoop();
			}
			this.gameId = ((NewGameMessage)message).getGameId();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	////////COMMUNICATE WITH SERVER///////////
	//this runs continuously once it starts, gets messages from the server
	private void listen() {
		while (true) {
			try {
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				if (values.hasNextValue()) {
					Message message = values.next();
					this.processMessageFromServer(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//also runs continuously
	private void playLoop() throws JsonParseException, JsonMappingException, IOException {
		if (actionsForServer.isEmpty()) {
			return;
		} else {
			try {
				UseableActionMessage action = actionsForServer.get(0);
				if (action.isTargetRequired()) {
					this.send(new RequestTargetsMessage(gameId, action.getId(), action.getAction()));
				}
				else {
					this.send(new UseAbilityMessage(gameId, action.getId(), action.getAction(), action.getTargetId()));
				}
			} catch (NumberFormatException | IndexOutOfBoundsException ex) {
				System.out.println("Not a valid action");
			}
		}
		//print("--------------------------------------------");
		//print("Game over!");
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
	
	//////////RENDER INFORMATION////////////
	private void processMessageFromServer(Message message) {
		serverMessages.getItems().add(message.toString());
		
		/*
		if (message instanceof UseableActionMessage) {
			actions.add((UseableActionMessage)message);
		} else if (message instanceof CardInfoMessage) {
			cards.add((CardInfoMessage)message);
		} else {
			serverMessages.getItems().add(message.toString());
		}
		*/
	}
	
	//need to find the right place to call this (probably multiple)
	//such as when the player has received all available usable actions
	//and after they send a message to the server
	private void render() {
		for (UseableActionMessage message : actions) {
			//create a box for each usable action
		}
		actions.clear();
		
		for (CardInfoMessage message : cards) {
			//render cards to their zones
		}
		cards.clear();
	}
	
}

