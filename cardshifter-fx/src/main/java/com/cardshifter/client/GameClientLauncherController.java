package com.cardshifter.client;

import com.cardshifter.client.buttons.AIChoiceButton;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

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

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.fx.FXMLGameController;
import com.cardshifter.modapi.ai.AIComponent;

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
	
	private static final String CONF_NAME = "name";

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
		this.aiChoices.put("Idiot", new AIComponent(new ScoringAI(AIs.idiot())));
		this.aiChoices.put("Loser", new AIComponent(new ScoringAI(AIs.loser())));
		this.aiChoices.put("Medium", new AIComponent(new ScoringAI(AIs.medium())));
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
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("SinglePlayerDocument.fxml"));
			Parent root = loader.load();
			
			FXMLGameController controller = loader.<FXMLGameController>getController();
			
			if (this.aiChoice != null) {
				controller.acceptAIChoice(this.aiChoice);
				controller.initializeGame();
			
				this.closeWithSuccess();
		
				Scene scene = new Scene(root);
				Stage stage = new Stage();
		
				stage.setScene(scene);
				stage.show();
			} else {
				System.out.println("AI not selected!");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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