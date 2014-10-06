package com.cardshifter.client;

import com.cardshifter.api.both.InviteRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class InviteWindow {
	
	@FXML private AnchorPane rootPane;
	@FXML private Label nameLabel;
	@FXML private AnchorPane noButton;
	@FXML private AnchorPane yesButton;
	
	private final InviteRequest message;
	private final GameClientLobby lobby;
	
	public InviteWindow(InviteRequest message, GameClientLobby lobby) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("InviteWindowDocument.fxml"));
            loader.setController(this);
			loader.load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.message = message;
		this.lobby = lobby;
		
		this.nameLabel.setText(message.getName());
		this.yesButton.setOnMouseClicked(lobby::acceptGameRequest);
		this.noButton.setOnMouseClicked(lobby::declineGameRequest);
	}
	
	public AnchorPane getRootPane() {
		return this.rootPane;
	}
}
