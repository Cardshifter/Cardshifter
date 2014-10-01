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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public final class GameClientLauncherController implements Initializable {
	
	@FXML private TextField ipAddressBox;
	@FXML private TextField portBox;
	@FXML private Button connectButton;
	@FXML private Label errorMessage;
	@FXML private AnchorPane anchorPane;

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
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		connectButton.setOnAction(this::buttonClick);
		ipAddressBox.setText("127.0.0.1");
		portBox.setText("4242");
	}		
	
}