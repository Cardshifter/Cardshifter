
package com.cardshifter.client.views;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ActionButton extends Group {
	
	private final double sizeX;
	private final double sizeY;
	
	public ActionButton(String label, double sizeX, double sizeY, Runnable onClick) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.setUpRectangle();
		this.setUpLabel(label);
		this.setOnMouseClicked(e -> onClick.run());
	}
	
	private void setUpRectangle() {
		Rectangle actionBack = new Rectangle(0, 0, this.sizeX, this.sizeY);
		actionBack.setFill(Color.BLUEVIOLET);
		this.getChildren().add(actionBack);
	}
	
	private void setUpLabel(String label) {
		Label actionLabel = new Label();
		actionLabel.setText(label);
		actionLabel.relocate(this.sizeX/2.5, 0);
		this.getChildren().add(actionLabel);
	}
	
}
