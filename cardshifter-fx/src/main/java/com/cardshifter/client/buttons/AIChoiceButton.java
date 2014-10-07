package com.cardshifter.client.buttons;

import com.cardshifter.client.GameClientLauncherController;
import javafx.scene.input.MouseEvent;

public class AIChoiceButton extends GenericButton {
	
	private final GameClientLauncherController controller;
	
	public AIChoiceButton (double sizeX, double sizeY, String aiName, GameClientLauncherController controller) {
		super(sizeX, sizeY, aiName);
		this.controller = controller;
		
		super.setUpRectangle();
		super.setUpLabel();
		
		this.setOnMouseClicked(this::actionButtonClicked);
	}
	
	@Override
	public void actionButtonClicked(MouseEvent event) {
		this.controller.clearAIButtons();
		this.controller.setAI(super.getButtonString());
		this.highlightButton();
	}
	
}
