package com.cardshifter.fx;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.actions.TargetSet;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.cards.CardComponent;

public class ChoiceBoxNode {
	
	private final double sizeX;
	private final double sizeY;
	private final String name;
	private final ECSAction action;
	private final FXMLGameController controller;
	private final Entity performer;
	
	private final Group choiceBoxGroup;
	
	public ChoiceBoxNode(double sizeX, double sizeY, String name, ECSAction action, FXMLGameController controller) {
		this.performer = controller.getPlayerPerspective();
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.name = name;
		this.action = action;
		this.controller = controller;
		this.choiceBoxGroup = new Group();
		this.createChoiceBox();
	}
	
	public Group getChoiceBoxGroup() {
		return this.choiceBoxGroup;
	}
	
	private void createChoiceBox() {
		this.createChoiceBoxBackground();
		this.createChoiceBoxFace();
		this.createChoiceBoxText();
		this.createActivateButton();
	}
	
	private void createChoiceBoxBackground() {
		//could be changed to be some kind of animated background or jpeg
		Rectangle activeBackground = new Rectangle(-this.sizeX*0.02, -this.sizeX*0.02, this.sizeX, this.sizeY);
		activeBackground.setFill(Color.YELLOW);
		choiceBoxGroup.getChildren().add(activeBackground);
	}
	
	private void createChoiceBoxFace() {
		Rectangle choiceBoxFace = new Rectangle(0,0,this.sizeX*0.96,this.sizeY*0.96);
		choiceBoxFace.setFill(Color.FIREBRICK);
		choiceBoxGroup.getChildren().add(choiceBoxFace);
	}
	
	private void createChoiceBoxText() {
		Label choiceBoxText = new Label();
		choiceBoxText.setText(String.format("Action = \n %s", this.action.getName()));
		choiceBoxText.setTextFill(Color.WHITE);
		choiceBoxText.relocate(this.sizeX/2,this.sizeY/2);
		choiceBoxGroup.getChildren().add(choiceBoxText);
	}
   
	private void createActivateButton() {
		Button button = new Button();
		//button.setStyle("-fx-background-color:transparent");
		//button.minWidth(100);
		//button.minHeight(100);
		//button.prefWidth(100);
		//button.prefHeight(100);
		button.setOnAction(this::buttonClick);
		choiceBoxGroup.getChildren().add(button);
	}
	
	private void buttonClick(ActionEvent event) {
		System.out.println("Trying to Perform Choice");
		if (this.action.isAllowed(performer)) {
			if (!this.action.getTargetSets().isEmpty()) {
				TargetSet targetAction = this.action.getTargetSets().get(0);
				List<Entity> targets = targetAction.findPossibleTargets();
				if (targets.isEmpty()) {
					return;
				}
				
				List<Entity> targetCards = new ArrayList<>();
				for (Entity target : targets) {
					if (target.hasComponent(CardComponent.class)) {
						targetCards.add(target);
					} else {
						//attacking the other player here
						targetAction.addTarget(target);
						action.perform(performer);
						this.controller.createData();
					}
				}
				this.controller.markTargets(targetCards);
				this.controller.nextAction = action;
			} else {
				this.action.perform(performer);
				this.controller.createData();
			}
		}
		this.controller.render();
	}
}
