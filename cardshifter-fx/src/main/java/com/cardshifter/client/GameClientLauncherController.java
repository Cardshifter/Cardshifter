package com.cardshifter.client;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.cardshifter.api.config.DeckConfig;

import com.cardshifter.ai.FakeAIClientTCG;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.api.ClientIO;
import com.cardshifter.api.ClientServerInterface;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.client.buttons.AIChoiceButton;
import com.cardshifter.core.game.FakeClient;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;

public final class GameClientLauncherController implements Initializable {
	
	@FXML private TextField ipAddressBox;
	@FXML private TextField portBox;
	@FXML private TextField userNameBox;
	@FXML private Button connectButton;
	@FXML private Label errorMessage;
	@FXML private AnchorPane anchorPane;
	@FXML private Button localGameButton;
	@FXML private HBox aiChoiceBox;
	
	private final Map<String, AIComponent> aiChoices = new HashMap<>();
	private AIComponent aiChoice;
	private final Preferences settings = Preferences.userNodeForPackage(GameClientLauncherController.class);
	private ClientIO human;
	private final ModCollection mods = new ModCollection();
	
	private static final String CONF_NAME = "name";
	private static final String DEFAULT_MOD = CardshifterConstants.VANILLA;

	private String getCharactersFromTextField(TextField textField) {
		return textField.getCharacters().toString();
	}
	
	private void buttonClick(ActionEvent event) {
		String ipAddressValue = this.getCharactersFromTextField(this.ipAddressBox);
		int portValue = Integer.parseInt(this.getCharactersFromTextField(this.portBox));
		String userNameValue = this.getCharactersFromTextField(this.userNameBox);
		settings.put(CONF_NAME, userNameValue);
		//this.switchToMainGameWindow(ipAddressValue, portValue);
		this.switchToLobbyWindow(ipAddressValue, portValue, userNameValue);
	}
	
	private void closeWithSuccess() {
		Node source = anchorPane;
		Stage stage = (Stage)source.getScene().getWindow();
		stage.close();
	}
	
	private void switchToLobbyWindow(String ipAddress, int port, String userName) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyDocument.fxml"));
			Parent root = (Parent)loader.load();
			
			GameClientLobby controller = loader.<GameClientLobby>getController();
			controller.acceptConnectionSettings(ipAddress, port, userName);
			
			if (controller.connectToLobby()) {
				errorMessage.setText("Success!");
				this.closeWithSuccess();
				
				Scene scene = new Scene(root);
				Stage lobbyStage = new Stage();
				lobbyStage.setScene(scene);
				lobbyStage.setOnCloseRequest(windowEvent -> controller.closeLobby());
				lobbyStage.show();
			} else {
				errorMessage.setText("Connection Failed!");
			}
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}	
	
	private void createAIChoices() {
		mods.getAIs().forEach((name, ai) -> aiChoices.put(name, new AIComponent(ai)));
		localGameButton.setOnAction(this::localGameStart);
		this.createAIButtons();
	}
	
	private void createAIButtons() {
		double buttonWidth = this.aiChoiceBox.getPrefWidth() / this.aiChoices.size();
		double buttonHeight = this.aiChoiceBox.getPrefHeight() - this.aiChoiceBox.getPrefHeight()/4;
		for (String string : this.aiChoices.keySet()) {
			AIChoiceButton button = new AIChoiceButton(buttonWidth, buttonHeight, string, this);
			this.aiChoiceBox.getChildren().add(button);
		}
	}
	
	public void clearAIButtons() {
		for (Object button : this.aiChoiceBox.getChildren()) {
			((AIChoiceButton)button).unHighlightButton();
		}
	}
	
	public void setAI(String aiName) {
		this.aiChoice = this.aiChoices.get(aiName);
	}
	
	private void localGameStart(ActionEvent event) {
		ECSMod mod = mods.getModFor(DEFAULT_MOD);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		TCGGame game = new TCGGame(() -> executor, DEFAULT_MOD, 1, mod);
		ClientServerInterface singlePlayerHandler = new ClientServerInterface() {
			@Override
			public void performIncoming(Message message, ClientIO clientIO) {
				if (message instanceof UseAbilityMessage) {
					UseAbilityMessage msg = (UseAbilityMessage) message;
					game.handleMove(msg, clientIO);
				}
				if (message instanceof RequestTargetsMessage) {
					RequestTargetsMessage msg = (RequestTargetsMessage) message;
					game.informAboutTargets(msg, clientIO);
				}
				if (message instanceof PlayerConfigMessage) {
					PlayerConfigMessage msg = (PlayerConfigMessage) message;
					game.incomingPlayerConfig(msg, clientIO);
				}
			}
			
			@Override
			public void onDisconnected(ClientIO clientIO) {
			}
			
			@Override
			public int newClientId() {
				return 0;
			}
			
			@Override
			public void handleMessage(ClientIO clientIO, String message) {
				throw new UnsupportedOperationException();
			}
		};
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientDocument.fxml"));
			Parent root = (Parent)loader.load();
			GameClientController controller = loader.<GameClientController>getController();
			human = new FakeClient(singlePlayerHandler, msg -> handleMessage(msg, controller));
			ClientIO ai = new FakeAIClientTCG(singlePlayerHandler, this.aiChoice.getAI());
			game.start(Arrays.asList(human, ai));
			Entity humanPlayer = game.playerFor(human);
			PlayerComponent playerInfo = humanPlayer.getComponent(PlayerComponent.class);
			controller.acceptConnectionSettings(new NewGameMessage(0, playerInfo.getIndex()), e -> human.sentToServer(e));
			
			Scene scene = new Scene(root);
			Stage gameStage = new Stage();
			gameStage.setScene(scene);
			gameStage.setOnCloseRequest(windowEvent -> {});
			gameStage.show();
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private void handleMessage(Message msg, GameClientController controller) {
		if (msg instanceof PlayerConfigMessage) {
			PlayerConfigMessage configMessage = (PlayerConfigMessage) msg;
			
			Map<String, Object> configs = configMessage.getConfigs();
			for (Entry<String, Object> entry : configs.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof DeckConfig) {
					DeckConfig deckConfig = (DeckConfig) value;
					this.showDeckBuilderWindow(configMessage, deckConfig, true);
				}
			}		
			return;
		}
		Platform.runLater(() -> controller.processMessageFromServer(msg));
	}

	private void showDeckBuilderWindow(PlayerConfigMessage conf, DeckConfig deckConfig, boolean startingGame) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("DeckBuilderDocument.fxml"));
			Parent root = (Parent)loader.load();
			DeckBuilderWindow controller = loader.<DeckBuilderWindow>getController();
			
			controller.acceptDeckConfig(deckConfig, cnf -> human.sentToServer(incorporateConfig(conf, deckConfig, cnf)));
			controller.configureWindow();
			
			if (!startingGame) {
				controller.disableGameStart();
			}
			
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setScene(scene);
//			stage.setOnCloseRequest(windowEvent -> this.closeDeckBuilderWindow());
			stage.show();
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private Message incorporateConfig(PlayerConfigMessage conf, DeckConfig oldConfig, DeckConfig newConfig) {
		Map<String, Object> configs = conf.getConfigs();
		
		for (Entry<String, Object> entry : configs.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof DeckConfig) {
				DeckConfig config = (DeckConfig) value;
				newConfig.getChosen().forEach((id, count) -> config.setChosen(id, count));
			}
		}
		
		return new PlayerConfigMessage(conf.getGameId(), configs);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.connectButton.setOnAction(this::buttonClick);
		this.ipAddressBox.setText("127.0.0.1");
		this.portBox.setText("4242");
		this.userNameBox.setText(settings.get(CONF_NAME, "Enter Name"));
		this.createAIChoices();
	}	

}