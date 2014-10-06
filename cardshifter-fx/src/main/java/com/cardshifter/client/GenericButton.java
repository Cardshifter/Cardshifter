package com.cardshifter.client;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GenericButton extends Group {
	
	private final double sizeX;
	private final double sizeY;
	private final String aiName;
	private Rectangle messageBackground;
	private final GameClientLauncherController controller;
	
	public GenericButton (double sizeX, double sizeY, String aiName, GameClientLauncherController controller) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.aiName = aiName;
		this.controller = controller;
		
		this.setUpRectangle();
		this.setUpLabel();
		
		this.setOnMouseClicked(this::actionButtonClicked);
	}
	
	private void setUpRectangle() {
		this.messageBackground = new Rectangle(0, 0, this.sizeX, this.sizeY);
		this.messageBackground.setFill(Color.BLUEVIOLET);
		this.getChildren().add(this.messageBackground);
		
		Rectangle messageForeground = new Rectangle(0, 0, this.sizeX * 0.90, this.sizeY * 0.80);
		messageForeground.relocate(this.sizeX * 0.05, this.sizeY * 0.10);
		messageForeground.setFill(Color.BLACK);
		this.getChildren().add(messageForeground);
	}
	
	private void setUpLabel() {
		Label buttonLabel = new Label();
		buttonLabel.setText(this.aiName);
		buttonLabel.setTextFill(Color.WHITE);
		buttonLabel.relocate(this.sizeX/3, 0);
		this.getChildren().add(buttonLabel);
	}
	
	private void actionButtonClicked(MouseEvent event) {
		System.out.println("heya");
		this.controller.clearAIButtons();
		this.controller.setAI(this.aiName);
		this.highlightButton();
	}
	
	private void highlightButton() {
		this.messageBackground.setFill(Color.YELLOW);
	}
	
	public void unHighlightButton() {
		this.messageBackground.setFill(Color.BLUEVIOLET);
	}
}
