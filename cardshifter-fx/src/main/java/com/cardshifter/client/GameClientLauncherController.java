package com.cardshifter.client;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.fx.FXMLGameController;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
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
import net.zomis.cardshifter.ecs.ai.AIComponent;

public final class GameClientLauncherController implements Initializable {
	
	@FXML private TextField ipAddressBox;
	@FXML private TextField portBox;
	@FXML private Button connectButton;
	@FXML private Label errorMessage;
	@FXML private AnchorPane anchorPane;
	@FXML private Button localGameButton;
	@FXML private HBox aiChoiceBox;
	
	private Map<String, AIComponent> aiChoices = new HashMap<>();
	private AIComponent aiChoice;

	private String getCharactersFromTextField(TextField textField) {
		return textField.getCharacters().toString();
	}
	
	private void buttonClick(ActionEvent event) {
		String ipAddressValue = this.getCharactersFromTextField(ipAddressBox);
		int portValue = Integer.parseInt(this.getCharactersFromTextField(portBox));
		this.switchToMainGameWindow(ipAddressValue, portValue);
	}
	
	private void closeWithSuccess() {
		Node source = anchorPane;
		Stage stage = (Stage)source.getScene().getWindow();
		stage.close();
	}
	
	private void switchToMainGameWindow(String ipAddress, int port) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientDocument.fxml"));
			Parent root = (Parent)loader.load();
			
			GameClientController controller = loader.<GameClientController>getController();
			controller.acceptIPAndPort(ipAddress, port);
			
			if (controller.connectToGame()) {
				errorMessage.setText("Success!");
				this.closeWithSuccess();
				
				Scene scene = new Scene(root);
				Stage gameStage = new Stage();
				gameStage.setScene(scene);
				gameStage.setOnCloseRequest(windowEvent -> controller.closeGame());
				gameStage.show();
			} else {
				errorMessage.setText("Connection Failed!");
			}
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	private void createAIChoices() {
		this.aiChoices.put("Idiot", new AIComponent(new ScoringAI(AIs.loser())));
		this.aiChoices.put("Loser", new AIComponent(new ScoringAI(AIs.loser())));
		this.aiChoices.put("Medium", new AIComponent(new ScoringAI(AIs.medium())));
		localGameButton.setOnAction(this::localGameStart);
		this.createAIButtons();
	}
	
	private void createAIButtons() {
		for (String string : this.aiChoices.keySet()) {
			GenericButton button = new GenericButton(this.aiChoiceBox.getPrefWidth() / this.aiChoices.size(), this.aiChoiceBox.getPrefHeight() / this.aiChoices.size(), string, this);
			this.aiChoiceBox.getChildren().add(button);
		}
	}
	
	public void clearAIButtons() {
		for (Object button : this.aiChoiceBox.getChildren()) {
			((GenericButton)button).unHighlightButton();
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
		connectButton.setOnAction(this::buttonClick);
		ipAddressBox.setText("127.0.0.1");
		portBox.setText("4242");
		this.createAIChoices();
	}	
}