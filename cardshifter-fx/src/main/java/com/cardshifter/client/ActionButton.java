
package com.cardshifter.client;

import com.cardshifter.server.outgoing.UseableActionMessage;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ActionButton extends Group {
	
	private final UseableActionMessage message;
	private final GameClientController controller;
	private final double sizeX;
	private final double sizeY;
	
	public ActionButton(UseableActionMessage message, GameClientController controller, double sizeX, double sizeY) {
		this.message = message;
		this.controller = controller;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.setUpRectangle();
		this.setUpLabel();
		this.setOnMouseClicked(this::actionButtonClicked);
	}
	private void setUpRectangle() {
		Rectangle actionBack = new Rectangle(0, 0, this.sizeX, this.sizeY);
		actionBack.setFill(Color.BLUEVIOLET);
		this.getChildren().add(actionBack);
	}
	private void setUpLabel() {
		Label actionLabel = new Label();
		actionLabel.setText(this.message.getAction());
		actionLabel.relocate(this.sizeX/2.5, 0);
		this.getChildren().add(actionLabel);
	}
	
	private void actionButtonClicked(MouseEvent event) {
		this.controller.createAndSendMessage(this.message);
	}
	
}
