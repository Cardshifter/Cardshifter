package com.cardshifter.client;

import java.net.URL;
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
import javafx.stage.Stage;

//This class just loads the FXML document which initializes its DocumentController

public class GameClientLauncherController implements Initializable {
	@FXML
	private TextField ipAddressBox;
	@FXML
	private TextField portBox;
	@FXML
	private Button connectButton;
	@FXML
	private Label errorMessage;

	public GameClientLauncherController() throws Exception {
	}
	
	private void setUpButton() {
		connectButton.setOnAction(this::buttonClick);
	}
	
	private String getCharactersFromTextField(TextField textField) {
		return textField.getCharacters().toString();
	}
	
	private void buttonClick(ActionEvent event) {
		//Get values from the TextFields
		String ipAddressValue = this.getCharactersFromTextField(ipAddressBox);
		int portValue = Integer.parseInt(this.getCharactersFromTextField(portBox));
		
		//Attempt to make a connection
		try {
			//Send a test to the server, to make sure that it is valid
			
			//if it is valid
			errorMessage.setText("Success!");
			this.switchToMainGameWindow(ipAddressValue, portValue);
			this.closeWithSuccess(event);
			
		} catch (Exception e) {
			String message = e.getMessage();
			errorMessage.setText(message);
		}
	}
	
	private void switchToMainGameWindow(String ipAddress, int port) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientDocument.fxml"));
			
			Parent root = (Parent)loader.load();
			
			GameClientController controller = loader.<GameClientController>getController();
			controller.acceptIPAndPort(ipAddress, port);
			controller.connectToGame();
		
			Scene scene = new Scene(root);
			
			Stage gameStage = new Stage();
		
			gameStage.setScene(scene);
			//stage.centerOnScreen();
			gameStage.show();
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	//Does not currently close the window
	private void closeWithSuccess(ActionEvent event) {
		Node source = (Node)event.getSource();
		Stage stage = (Stage)source.getScene().getWindow();
		stage.close();
	}
	
	 // @param args the command line arguments
	
	//BOILERPLATE
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
		this.setUpButton();
	}		
	
}