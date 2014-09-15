package com.cardshifter.client;

import com.cardshifter.console.NetworkConsoleController;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

//This class just loads the FXML document which initializes its DocumentController

public class GameClientLauncher implements Initializable {
	@FXML
	private TextField ipAddressBox;
	@FXML
	private TextField portBox;
	@FXML
	private Button connectButton;
	@FXML
	private Label errorMessage;

	public GameClientLauncher() throws Exception {
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
			NetworkConsoleController control = new NetworkConsoleController(ipAddressValue, portValue);
			control.play(new Scanner(System.in));
			errorMessage.setText("Success!");
		} catch (Exception e) {
			String message = e.getMessage();
			errorMessage.setText(message);
		}
	}
	
	 // @param args the command line arguments
	
	//BOILERPLATE
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
		this.setUpButton();
	}		
	
}