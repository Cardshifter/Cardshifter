package com.cardshifter.fx;

import java.util.ArrayList;
import java.util.List;

import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.resources.Resources;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/* 
   The purpose of this class is to take in certain values from the 
   Game controller and create a Group that the controller can 
   retrieve in order to render a Player card on the screen
*/

public class CardNode {
	
	private final double sizeX;
	private final double sizeY;
//	private final String name;
	private final Entity card;
	private final FXMLGameController controller;
	private final Entity performer;
	
	private final Group cardGroup;
	
	public CardNode(Pane pane, int numCards, String name, Entity card, FXMLGameController controller) {
		this.performer = controller.getPlayerPerspective();
		
		//calculate card width based on pane size
		double paneWidth = pane.getPrefWidth();
		//reduce card size if there are over a certain amount of them
		int maxCards = Math.max(numCards, 10);
		double cardWidth = paneWidth / maxCards;
		
		this.sizeX = cardWidth;
		this.sizeY = pane.getPrefHeight();
//		this.name = name;
		this.card = card;
		this.controller = controller;
		this.cardGroup = new Group();
		this.createCard();
	}
	
	public Group getCardGroup() {
		return this.cardGroup;
	}
	
	public double getWidth() {
		return this.sizeX;
	}
	
	private void createCard() {
		this.createCardBackground();
		this.createCardArt();
		this.createCardIDLabel();
		this.createCardPropertyLabelsGroup();
		
		//only make the button when the card is active
		if(this.isCardActive() == true) {
			this.createCardActivateButton();
		}
	}
	
	private void createCardBackground() {
		Rectangle activeBackground = new Rectangle(-10,-10,this.sizeX, this.sizeY);
		activeBackground.setFill(Color.BLACK);
		if(this.isCardActive() == true) {
			activeBackground.setFill(Color.YELLOW);
		}
		cardGroup.getChildren().add(activeBackground);
	}
	
	private void createCardArt() {
		Rectangle cardBack = new Rectangle(0,0,this.sizeX*0.90,this.sizeY*0.90);
		cardBack.setFill(Color.FIREBRICK);
		cardGroup.getChildren().add(cardBack);
	}
	
	private void createCardIDLabel() {
		Label cardIdLabel = new Label();
		cardIdLabel.setText(String.format("CardID = %d", card.getId()));
		cardIdLabel.relocate(this.sizeX*0.15,0); //moving this over so it is out of the way of the temporary button
		cardIdLabel.setTextFill(Color.WHITE);
		cardGroup.getChildren().add(cardIdLabel);
	}
	
	private void createCardPropertyLabelsGroup() {
		//This gets the text from Lua and makes labels for each property
		int stringIndex = 0;
		List<String> stringList = new ArrayList<>();
		Resources.processResources(card, data -> stringList.add(data.getResource() + ": " + data.get()));
		for (String string : stringList) {			 
			Label cardStringLabel = new Label();
			
			if (string.equals("ATTACK_AVAILABLE: 1")) {
				string = "ATTACKS: 1";
			}
			
			cardStringLabel.setText(string);
			cardStringLabel.relocate(0, 25 + (stringIndex * 25));
			cardStringLabel.setTextFill(Color.WHITE);
			cardGroup.getChildren().add(cardStringLabel);
			stringIndex++;
		}
	}
	
	private void createCardActivateButton() {
		Button button = new Button();
		//button.setStyle("-fx-background-color:transparent");
		//button.minWidth(100);
		//button.minHeight(100);
		//button.prefWidth(100);
		//button.prefHeight(100);
		button.setOnAction(this::buttonClick);
		cardGroup.getChildren().add(button);
	}
	
	private void buttonClick(ActionEvent event) {
		System.out.println("Trying to Perform Action");
		List<ECSAction> cardActions = Actions.getPossibleActionsOn(card, performer);
		
		//If there is more than one action, create the choice box
		if(cardActions.size() > 1) {
			this.controller.buildChoiceBoxPane(card, cardActions);
		} else if (cardActions.size() == 1) {
			for (ECSAction action : cardActions) {
				if (action.isAllowed(performer)) {
					if (!action.getTargetSets().isEmpty()) {
						TargetSet targetAction = action.getTargetSets().get(0);
						List<Entity> targets = targetAction.findPossibleTargets();
						if (targets.isEmpty()) {
							return;
						}

						List<Entity> targetCards = new ArrayList<>();
						for (Entity target : targets) {
							if (target.hasComponent(CardComponent.class)) {
								targetCards.add(target);
							}
						}
						this.controller.markTargets(targetCards);
						this.controller.nextAction = action;
					} else {
						action.perform(performer);
						this.controller.createData();
					} 
				}
				this.controller.render();
			}
		}
	}
	
	private boolean isCardActive() {
		List<ECSAction> cardActions = Actions.getPossibleActionsOn(card, performer);
		return !cardActions.isEmpty();
	}
}
