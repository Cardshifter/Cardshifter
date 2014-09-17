package com.cardshifter.client;

import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.RequestTargetsMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.CardInfoMessage;
import com.cardshifter.server.outgoing.NewGameMessage;
import com.cardshifter.server.outgoing.PlayerMessage;
import com.cardshifter.server.outgoing.UpdateMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.cardshifter.server.outgoing.WaitMessage;
import com.cardshifter.server.outgoing.WelcomeMessage;
import com.cardshifter.server.outgoing.ZoneMessage;
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

public class GameClientController {
	
	//@FXML private AnchorPane rootPane;
	@FXML private Label serverMessage;
	@FXML private ListView<String> serverMessages;
	@FXML private Label opponentLife;
	@FXML private Label opponentCurrentMana;
	@FXML private Label opponentTotalMana;
	@FXML private Label opponentScrap;
	@FXML private Label playerLife;
	@FXML private Label playerCurrentMana;
	@FXML private Label playerTotalMana;
	@FXML private Label playerScrap;

	private final ObjectMapper mapper = new ObjectMapper();
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final List<UseableActionMessage> actions = Collections.synchronizedList(new ArrayList<>());
	private final List<CardInfoMessage> cards = Collections.synchronizedList(new ArrayList<>());
	private final List<Message> genericMessages = Collections.synchronizedList(new ArrayList<>());
	private final List<UseableActionMessage> actionsForServer = Collections.synchronizedList(new ArrayList<>());
	
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private String ipAddress;
	private int port;
	private int gameId;
	
	/////////INITIALIZATION///////////////
	public void acceptIPAndPort(String ipAddress, int port) {
		// this is passed into the object after it is automatically created by the FXML document
		this.ipAddress = ipAddress;
		this.port = port;
	}
	public void connectToGame() {
		// this is called on the object from the Game launcher before the scene is displayed
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
	public void play() {
		// this method only runs once at the start
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
				String displayMessage = ((WaitMessage)message).getMessage();
				Platform.runLater(() -> serverMessage.setText(displayMessage));
				
				message = messages.take();
			}
			this.gameId = ((NewGameMessage) message).getGameId();
			this.playLoop();
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
				while (values.hasNextValue()) {
					Message message = values.next();
					messages.offer(message);
					Platform.runLater(() -> this.processMessageFromServer(message));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void playLoop() throws JsonParseException, JsonMappingException, IOException {
		while (true) {
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
	
	//////////RENDER INFORMATION////////////
	private void processMessageFromServer(Message message) {
		
		serverMessages.getItems().add(message.toString());
	
		//do a check a prune server messages if it is too large - not in this method?
		
		System.out.println(message.toString());
		
		if (message instanceof PlayerMessage) {
			this.processPlayerMessage((PlayerMessage)message);
		} else if (message instanceof UpdateMessage) {
			this.processUpdateMessage((UpdateMessage)message);
		} else if (message instanceof ZoneMessage) {
			this.processZoneMessage((ZoneMessage)message);
		} else if (message instanceof CardInfoMessage) {
			this.processCardInfoMessage((CardInfoMessage)message);
		}
		
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
	
	/*
	Player Info: Player1 - {scrap=0, mana=1, manaMax=1, battlefield={Zone Battlefield (0) owned by {Player 'Player1'}}, deck={Zone Deck (47) owned by {Player 'Player1'}}, cardType=Player, life=10, hand={Zone Hand (5) owned by {Player 'Player1'}}}
Player Info: Player2 - {scrap=0, mana=0, manaMax=0, battlefield={Zone Battlefield (0) owned by {Player 'Player2'}}, deck={Zone Deck (47) owned by {Player 'Player2'}}, cardType=Player, life=10, hand={Zone Hand (5) owned by {Player 'Player2'}}}
ZoneMessage [id=3, name=Battlefield, owner=0, size=0, known=true]
ZoneMessage [id=4, name=Deck, owner=0, size=47, known=false]
ZoneMessage [id=5, name=Hand, owner=0, size=5, known=false]
ZoneMessage [id=58, name=Battlefield, owner=1, size=0, known=true]
ZoneMessage [id=59, name=Deck, owner=1, size=47, known=false]
ZoneMessage [id=60, name=Hand, owner=1, size=5, known=true]
CardInfo: 62 in zone 60 - {strength=3, cardType=Creature, health=2, sickness=1, creatureType=B0T, enchantments=0, manaCost=2, attacksAvailable=1}
CardInfo: 92 in zone 60 - {strength=4, cardType=Creature, health=4, sickness=1, creatureType=Bio, enchantments=0, manaCost=5, attacksAvailable=1}
CardInfo: 85 in zone 60 - {enchStrength=0, scrapCost=3, cardType=Enchantment, manaCost=0, enchHealth=3}
CardInfo: 73 in zone 60 - {enchStrength=2, scrapCost=5, cardType=Enchantment, manaCost=0, enchHealth=2}
CardInfo: 93 in zone 60 - {strength=5, cardType=Creature, health=3, sickness=1, creatureType=Bio, enchantments=0, manaCost=5, attacksAvailable=1}
com.cardshifter.server.outgoing.ResetAvailableActionsMessage@772c7ecd
UpdateMessage [id=2, key=manaMax, value=1]
UpdateMessage [id=2, key=mana, value=1]
com.cardshifter.server.outgoing.ResetAvailableActionsMessage@4ef494aa
UseableActionMessage [id=2, action=End Turn, targetRequired=false, targetId=0]
	*/
	
	private void processPlayerMessage(PlayerMessage message) {
		if (message.getName().equals("Player1")) {
			for (String string : message.getProperties().keySet()) {
				if (string.equals("SCRAP")) {
					opponentScrap.setText(message.getProperties().get("SCRAP").toString());
				} else if (string.equals("MANA")) {
					opponentCurrentMana.setText(message.getProperties().get("MANA").toString());
				} else if (string.equals("MANA_MAX")) {
					opponentTotalMana.setText(message.getProperties().get("MANA_MAX").toString());
				} else if (string.equals("HEALTH")) {
					opponentLife.setText(message.getProperties().get("HEALTH").toString());
				}
			}
		} else if (message.getName().equals("Player2")) {
			for (String string : message.getProperties().keySet()) {
				if (string.equals("SCRAP")) {
					playerScrap.setText(message.getProperties().get("SCRAP").toString());
				} else if (string.equals("MANA")) {
					playerCurrentMana.setText(message.getProperties().get("MANA").toString());
				} else if (string.equals("MANA_MAX")) {
					playerTotalMana.setText(message.getProperties().get("MANA_MAX").toString());
				} else if (string.equals("HEALTH")) {
					playerLife.setText(message.getProperties().get("HEALTH").toString());
				}
			}
		}
	}
	private void processUpdateMessage(UpdateMessage message) {
		if (message.getKey().equals("test")) {
			
		}
	}
	private void processZoneMessage(ZoneMessage message) {
		
	}
	private void processCardInfoMessage(CardInfoMessage message) {
		
	}
	
	//need to find the right place to call this (probably multiple)
	//such as when the player has received all available usable actions
	//and after they send a message to the server
	/*
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
	*/
	
}

