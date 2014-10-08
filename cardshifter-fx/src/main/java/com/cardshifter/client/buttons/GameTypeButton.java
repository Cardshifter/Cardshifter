package com.cardshifter.client.buttons;

import com.cardshifter.client.GameClientLobby;
import javafx.scene.input.MouseEvent;

public class GameTypeButton extends GenericButton {

	private final GameClientLobby controller;
	
	public GameTypeButton (double sizeX, double sizeY, String gameType, GameClientLobby controller) {
		super (sizeX, sizeY, gameType);
		this.controller = controller;
		
		super.setUpRectangle();
		super.setUpLabel();
		
		this.setOnMouseClicked(this::actionButtonClicked);
	}
	
	@Override
	public void actionButtonClicked(MouseEvent event) {
		this.controller.clearGameTypeButtons();
		this.controller.setGameType(super.getButtonString());
		super.highlightButton();
	}
	
}
