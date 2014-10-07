package com.cardshifter.client.buttons;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GenericButton extends Group {
	
	private final double sizeX;
	private final double sizeY;
	private final String buttonString;
	private Rectangle buttonBackground;
	
	public GenericButton (double sizeX, double sizeY, String buttonString) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.buttonString = buttonString;
	}
	
	public String getButtonString() {
		return this.buttonString;
	}
	
	public void setUpRectangle() {
		this.buttonBackground = new Rectangle(0, 0, this.sizeX, this.sizeY);
		this.buttonBackground.setFill(Color.BLUEVIOLET);
		this.getChildren().add(this.buttonBackground);
		
		Rectangle buttonForeground = new Rectangle(0, 0, this.sizeX * 0.90, this.sizeY * 0.80);
		buttonForeground.relocate(this.sizeX * 0.05, this.sizeY * 0.10);
		buttonForeground.setFill(Color.BLACK);
		this.getChildren().add(buttonForeground);
	}
	
	public void setUpLabel() {
		Label buttonLabel = new Label();
		buttonLabel.setText(this.buttonString);
		buttonLabel.setTextFill(Color.WHITE);
		buttonLabel.setStyle(String.format("-fx-font-size:%f", this.sizeY - this.sizeY * 0.60));
		buttonLabel.relocate(this.sizeX/3, this.sizeY/3);
		this.getChildren().add(buttonLabel);
	}
	
	public void actionButtonClicked(MouseEvent event) {
		//Override in subclasses
	}
	
	public void highlightButton() {
		this.buttonBackground.setFill(Color.YELLOW);
	}
	
	public void unHighlightButton() {
		this.buttonBackground.setFill(Color.BLUEVIOLET);
	}
	
}
