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
import java.util.HashMap;
import java.util.List;
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
import javafx.scene.input.MouseEvent;
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
	private int playerId;
	private int playerIndex;
	private int opponentId;
	private int opponentHandId;
	private int opponentBattlefieldId;
	private int playerHandId;
	private int playerBattlefieldId;
	private Map<Integer, Pane> idMap = new HashMap<>();
	
	private int opponentHandSize;
	
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
	
	/*
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
	*/
	
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
		} else if (message instanceof NewGameMessage) {
			this.processNewGameMessage((NewGameMessage)message);
		} else if (message instanceof UseableActionMessage) {
			this.processUseableActionMessage((UseableActionMessage)message);
		}
	}
	
	private void processUseableActionMessage(UseableActionMessage message) {
		double paneHeight = actionBox.getHeight();
		double paneWidth = actionBox.getWidth();
		
		int maxActions = 5;
		double actionWidth = paneWidth / maxActions;
		
		ActionButton actionButton = new ActionButton(message, this, actionWidth, paneHeight);
		actionBox.getChildren().add(actionButton);
	}
	
	private void processNewGameMessage(NewGameMessage message) {
		this.playerIndex = message.getPlayerIndex();
		
		System.out.println(String.format("You are player: %d", this.playerIndex));
	}
	
	private void processPlayerMessage(PlayerMessage message) {
		if (message.getIndex() == this.playerIndex) {
			this.playerId = message.getId();
			this.processPlayerMessageForPlayer(message, playerStatBox);
		} else {
			this.opponentId = message.getId();
			this.processPlayerMessageForPlayer(message, opponentStatBox);
		}
	}
	private void processPlayerMessageForPlayer(PlayerMessage message, Pane statBox) {
		statBox.getChildren().clear();
		Map<String, Integer> sortedMap = new TreeMap<>(message.getProperties());
		for(Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			String key = entry.getKey();
			statBox.getChildren().add(new Label(key));
			int value = entry.getValue();
			statBox.getChildren().add(new Label(String.format("%d",value)));
		}
	}
	
	private void processUpdateMessage(UpdateMessage message) {
		if (message.getId() == this.playerId) {
			this.processUpdateMessageForPlayer(playerStatBox, message);
		} else if (message.getId() == this.opponentId) {
			this.processUpdateMessageForPlayer(opponentStatBox, message);
		}
	}
	private void processUpdateMessageForPlayer(Pane statBox, UpdateMessage message) {
	}
	
	//////////ZONE MESSAGES//////////////
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
	*/

