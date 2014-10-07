package com.cardshifter.client;

import com.cardshifter.api.both.InviteRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class InviteWindow {
	
	@FXML private AnchorPane rootPane;
	@FXML private Label nameLabel;
	@FXML private Label gameTypeLabel;
	@FXML private AnchorPane noButton;
	@FXML private AnchorPane yesButton;
	
	public InviteWindow(InviteRequest message, GameClientLobby lobby) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("InviteWindowDocument.fxml"));
            loader.setController(this);
			loader.load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.initializeLabels(message, lobby);
	}
	
	private void initializeLabels(InviteRequest message, GameClientLobby lobby) {
		this.nameLabel.setText(message.getName());
		this.gameTypeLabel.setText(message.getGameType() + "?");
		this.yesButton.setOnMouseClicked(lobby::acceptGameRequest);
		this.noButton.setOnMouseClicked(lobby::declineGameRequest);
	}

	public AnchorPane getRootPane() {
		return this.rootPane;
	}
}
