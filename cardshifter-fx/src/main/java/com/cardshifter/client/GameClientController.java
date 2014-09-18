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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameClientController {
	
	//@FXML private AnchorPane rootPane;
	@FXML private Label serverMessage;
	@FXML private ListView<String> serverMessages;
	@FXML private VBox opponentStatBox;
	@FXML private VBox playerStatBox;
	@FXML private HBox opponentHandPane;
	@FXML private HBox opponentBattlefieldPane;
	@FXML private HBox playerHandPane;
	@FXML private HBox playerBattlefieldPane;
	@FXML private HBox actionBox;

	private final ObjectMapper mapper = new ObjectMapper();
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private String ipAddress;
	private int port;
	
	private int gameId;
	private int playerId;
	private int playerIndex;
	private int opponentId;
	private int opponentHandId;
	private int opponentBattlefieldId;
	private int playerHandId;
	private int playerBattlefieldId;
	private final Map<Integer, Pane> idMap = new HashMap<>();
	private final Map<String, Integer> playerStatBoxMap = new HashMap<>();
	private final Map<String, Integer> opponentStatBoxMap = new HashMap<>();
	
	private int opponentHandSize;
	
	public void acceptIPAndPort(String ipAddress, int port) {
		// this is passed into the object after it is automatically created by the FXML document
		this.ipAddress = ipAddress;
		this.port = port;
	}
	public boolean connectToGame() {
		// this is called on the object from the Game launcher before the scene is displayed
		try {
			this.socket = new Socket(this.ipAddress, this.port);
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();
			mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			new Thread(this::listen).start();
		} catch (Exception e) {
			System.out.println("Connection Failed");
			return false;
		}
		
		new Thread(this::play).start();
		return true;
	}
	private void play() {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void listen() {
		while (true) {
			try {
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				while (values.hasNextValue()) {
					Message message = values.next();
					messages.offer(message);
					Platform.runLater(() -> this.processMessageFromServer(message));
				}
			} catch (SocketException e) {
				e.printStackTrace();
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
	
	public void createAndSendMessage(Message message) {
		try {
			UseableActionMessage action = (UseableActionMessage)message;
			
			if (action.isTargetRequired()) {
				this.send(new RequestTargetsMessage(gameId, action.getId(), action.getAction()));
			} else {
				this.send(new UseAbilityMessage(gameId, action.getId(), action.getAction(), action.getTargetId()));
			}
		} catch (NumberFormatException | IndexOutOfBoundsException ex) {
			System.out.println("Not a valid action");
		}
		
		this.actionBox.getChildren().clear();
	}
	
	private void processMessageFromServer(Message message) {
		
		serverMessages.getItems().add(message.toString());
		
		//this is for diagnostics so I can copy paste the messages to know their format
		System.out.println(message.toString());
		
		if (message instanceof NewGameMessage) {
			this.processNewGameMessage((NewGameMessage) message);
		} else if (message instanceof PlayerMessage) {
			this.processPlayerMessage((PlayerMessage)message);
		} else if (message instanceof ZoneMessage) {
			this.processZoneMessage((ZoneMessage)message);
		} else if (message instanceof CardInfoMessage) {
			this.processCardInfoMessage((CardInfoMessage)message);
		} else if (message instanceof UseableActionMessage) {
			this.processUseableActionMessage((UseableActionMessage)message);
		} else if (message instanceof UpdateMessage) {
			this.processUpdateMessage((UpdateMessage)message);
		} 
	}
	
	private void processNewGameMessage(NewGameMessage message) {
		this.playerIndex = message.getPlayerIndex();
		
		System.out.println(String.format("You are player: %d", this.playerIndex));
	}
	
	private void processPlayerMessage(PlayerMessage message) {
		if (message.getIndex() == this.playerIndex) {
			this.playerId = message.getId();
			this.processPlayerMessageForPlayer(message, playerStatBox, playerStatBoxMap);
		} else {
			this.opponentId = message.getId();
			this.processPlayerMessageForPlayer(message, opponentStatBox, opponentStatBoxMap);
		}
	}
	private void processPlayerMessageForPlayer(PlayerMessage message, Pane statBox, Map playerMap) {
		statBox.getChildren().clear();
		Map<String, Integer> sortedMap = new TreeMap<>(message.getProperties());
		playerMap.putAll(sortedMap);
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			String key = entry.getKey();
			statBox.getChildren().add(new Label(key));
			int value = entry.getValue();
			statBox.getChildren().add(new Label(String.format("%d",value)));
		}
	}
	
	private void processZoneMessage(ZoneMessage message) {
		if (!this.allZonesAssigned()) {
			this.assignZoneIdForZoneMessage(message);
		}

		Pane targetPane = this.idMap.get(message.getId());
		this.processZoneMessageForPane(targetPane, message);
	}
	private boolean allZonesAssigned() {
		return (this.idMap.containsValue(playerBattlefieldPane) 
				&& this.idMap.containsValue(playerHandPane)
				&& this.idMap.containsValue(opponentBattlefieldPane)
				&& this.idMap.containsValue(opponentHandPane));
	} 
	private void assignZoneIdForZoneMessage(ZoneMessage message) {
		if (!this.idMap.containsKey(message.getId())) {
			if (message.getName().equals("Battlefield")) {
				if(message.getOwner() == this.playerId) {
					this.playerBattlefieldId = message.getId();
					this.idMap.put(this.playerBattlefieldId, playerBattlefieldPane);
				} else {
					this.opponentBattlefieldId = message.getId();
					this.idMap.put(this.opponentBattlefieldId, opponentBattlefieldPane);
				}
			} else if (message.getName().equals("Hand")) {
				if(message.getOwner() == this.playerId) {
					this.playerHandId = message.getId();
					this.idMap.put(this.playerHandId, playerHandPane);
				} else {
					this.opponentHandId = message.getId();
					this.opponentHandSize = message.getSize();
					this.idMap.put(this.opponentHandId, opponentHandPane);
					
					this.renderOpponentHand();
				}
			}
		}
	}
	private void processZoneMessageForPane(Pane pane, ZoneMessage message) {
	}
	
	private void processCardInfoMessage(CardInfoMessage message) {
		if (idMap.containsKey(message.getZone())) {
			Pane targetPane = idMap.get(message.getZone());
		
			if (targetPane == opponentBattlefieldPane) {
				this.addCardToOpponentBattlefieldPane(message);
			} else if (targetPane == opponentHandPane) {
				this.addCardToOpponentHandPane(message);
			} else if (targetPane == playerBattlefieldPane) {
				this.addCardToPlayerBattlefieldPane(message);
			} else if (targetPane == playerHandPane) {
				this.addCardToPlayerHandPane(message);
			}
		}
	}	
	private void addCardToOpponentBattlefieldPane(CardInfoMessage message) {
	}
	private void addCardToOpponentHandPane(CardInfoMessage message) {
	}
	private void addCardToPlayerBattlefieldPane(CardInfoMessage message) {
	}
	private void addCardToPlayerHandPane(CardInfoMessage message) {
		CardHandDocumentController card = new CardHandDocumentController(message, this);
		playerHandPane.getChildren().add(card.getRootPane());
	}
	
	private void processUseableActionMessage(UseableActionMessage message) {
		double paneHeight = actionBox.getHeight();
		double paneWidth = actionBox.getWidth();
		
		int maxActions = 8;
		double actionWidth = paneWidth / maxActions;
		
		ActionButton actionButton = new ActionButton(message, this, actionWidth, paneHeight);
		actionBox.getChildren().add(actionButton);
	}
	
	private void processUpdateMessage(UpdateMessage message) {
		if (message.getId() == this.playerId) {
			this.processUpdateMessageForPlayer(playerStatBox, message, playerStatBoxMap);
		} else if (message.getId() == this.opponentId) {
			this.processUpdateMessageForPlayer(opponentStatBox, message, opponentStatBoxMap);
		}
	}
	private void processUpdateMessageForPlayer(Pane statBox, UpdateMessage message, Map playerMap) {
		String key = (String)message.getKey();
		Integer value = (Integer)message.getValue();
		playerMap.put(key, value);
	
		this.repaintStatBox(statBox, playerMap);
	}
	
	private void repaintStatBox(Pane statBox, Map<String, Integer> playerMap) {
		statBox.getChildren().clear();
		for (Map.Entry<String, Integer> entry : playerMap.entrySet()) {
			String key = entry.getKey();
			statBox.getChildren().add(new Label(key));
			int value = entry.getValue();
			statBox.getChildren().add(new Label(String.format("%d",value)));
		}
	}
	
	private void renderOpponentHand() {
		//Opponent cards are rendered differently because the faces are not visible
		double paneHeight = opponentHandPane.getHeight();
		double paneWidth = opponentHandPane.getWidth();
		
		int maxCards = 10;
		double cardWidth = paneWidth / maxCards;

		for (int currentCard = 0; currentCard < this.opponentHandSize; currentCard++) {
			Group cardGroup = new Group();
			Rectangle cardBack = new Rectangle(0,0,cardWidth,paneHeight);
			cardBack.setFill(Color.AQUAMARINE);
			cardGroup.getChildren().add(cardBack);
			opponentHandPane.getChildren().add(cardGroup);
		}
	}
}
	//Sample server message strings
	/*
	Sending: {"command":"login","username":"Player26"}
	Sending: {"command":"startgame"}
	com.cardshifter.server.outgoing.WelcomeMessage@33c7b4f6
	com.cardshifter.server.outgoing.NewGameMessage@2ff58dec
	You are player: 1
	Player Info: Player1 - {SCRAP=0, MANA=1, MANA_MAX=1, HEALTH=10}
	Player Info: Player2 - {SCRAP=0, MANA=0, HEALTH=10}
	ZoneMessage [id=4, name=Battlefield, owner=1, size=0, known=true]
	ZoneMessage [id=3, name=Hand, owner=1, size=5, known=false]
	ZoneMessage [id=2, name=Deck, owner=1, size=34, known=false]
	ZoneMessage [id=47, name=Battlefield, owner=44, size=0, known=true]
	ZoneMessage [id=46, name=Hand, owner=44, size=5, known=true]
	CardInfo: 48 in zone 46 - {SICKNESS=1, MANA_COST=1, ATTACK=1, HEALTH=1, ATTACK_AVAILABLE=1}
	CardInfo: 49 in zone 46 - {SICKNESS=1, MANA_COST=2, ATTACK=2, HEALTH=1, ATTACK_AVAILABLE=1}
	CardInfo: 50 in zone 46 - {SICKNESS=1, MANA_COST=3, ATTACK=3, HEALTH=3, ATTACK_AVAILABLE=1}
	CardInfo: 51 in zone 46 - {SICKNESS=1, MANA_COST=4, ATTACK=4, HEALTH=4, ATTACK_AVAILABLE=1}
	CardInfo: 52 in zone 46 - {SICKNESS=1, MANA_COST=5, ATTACK=5, HEALTH=5, ATTACK_AVAILABLE=1}
	ZoneMessage [id=45, name=Deck, owner=44, size=34, known=false]
	com.cardshifter.server.outgoing.ResetAvailableActionsMessage@2d326c35
	UpdateMessage [id=44, key=MANA_MAX, value=2]
	UpdateMessage [id=44, key=MANA, value=2]
	*/

