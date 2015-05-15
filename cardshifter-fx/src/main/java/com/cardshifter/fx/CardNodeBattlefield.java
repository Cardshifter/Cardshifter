package com.cardshifter.fx;

import java.util.List;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ResourceRetriever;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.zomis.cardshifter.ecs.usage.CyborgChroniclesGame.CyborgChroniclesResources;

public class CardNodeBattlefield extends Group {
	
	private final double sizeX;
	private final double sizeY;
//	private final String name;
	private final Entity card;
	private final FXMLGameController controller;
	private final boolean isPlayer;
	private final Entity performer;
	
	private static final ResourceRetriever resHealth = ResourceRetriever.forResource(CyborgChroniclesResources.HEALTH);
	private static final ResourceRetriever resAttack = ResourceRetriever.forResource(CyborgChroniclesResources.ATTACK);
	private static final ResourceRetriever resManaCost = ResourceRetriever.forResource(CyborgChroniclesResources.MANA_COST);
	private static final ComponentRetriever<ActionComponent> actions = ComponentRetriever.retreiverFor(ActionComponent.class);
	
	public CardNodeBattlefield (Pane pane, int numCards, String name, Entity card, FXMLGameController controller, boolean isPlayer) {
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
		this.isPlayer = isPlayer;
		this.createCard();
	}
	
	public Group getCardGroup() {
		return this;
	}
	
	public double getWidth() {
		return this.sizeX;
	}
	
	public Entity getCard() {
		return this.card;
	}
	
	private void createCard() {
		this.createCardBackground(false);
		this.createCardArt();
		this.createCardIDLabel();
		this.createCardPropertyLabelsGroup();
		
		//only make the button when the card is active
		if(this.isCardActive() == true) {
			if (this.isPlayer == true) {
				this.createCardActivateButton();
			}
		}
	}
	
	private void createCardBackground(boolean targetMode) {
		//background border will be smaller for these and a ratio
		Rectangle activeBackground = new Rectangle(-this.sizeX*0.02,-this.sizeY*0.02,this.sizeX, this.sizeY);
		activeBackground.setFill(Color.BLACK);
		
		//change the background color based on the state
		//this cannot be elseif because multiple states could be true
		if(this.isCardActive()) {
			activeBackground.setFill(Color.YELLOW);
		}
		if(this.canCardAttack()) {
			activeBackground.setFill(Color.GREEN);
		}
		if(targetMode) {
			activeBackground.setFill(Color.BLUE);
		}
		
		this.getChildren().add(activeBackground);
	}
	private boolean isCardActive() {
		return !Actions.getPossibleActionsOn(card, performer).isEmpty();
	}
	private boolean canCardAttack() {
		List<ECSAction> cardActions = Actions.getPossibleActionsOn(card, performer);
		return cardActions.stream().anyMatch(act -> act.getName().equals("Attack"));
	}
	
	private void createCardArt() {
		Rectangle cardBack = new Rectangle(0,0,this.sizeX*0.95,this.sizeY*0.95);
		cardBack.setFill(Color.FIREBRICK);
		this.getChildren().add(cardBack);
	}
	
	private void createCardIDLabel() {
		Label cardIdLabel = new Label();
		cardIdLabel.setText(String.format("CardID = %d", card.getId()));
		cardIdLabel.relocate(this.sizeX*0.15,0); //moving this over so it is out of the way of the temporary button
		cardIdLabel.setTextFill(Color.WHITE);
		this.getChildren().add(cardIdLabel);
	}
	
	private void createCardPropertyLabelsGroup() {
		//Only these values will be loaded from Lua, and only if they are contained in the data
		int health = resHealth.getFor(card);
		int strength = resAttack.getFor(card);
		int manaCost = resManaCost.getFor(card);
		
		//Need separate code for each label because they are in arbitrary locations
		Label strengthLabel = new Label();
		strengthLabel.setText(String.format("%d/", strength)); // do the "/" to have something between strength and health
		strengthLabel.relocate(this.sizeX*0.75,this.sizeY*0.80);
		strengthLabel.setTextFill(Color.WHITE);
		this.getChildren().add(strengthLabel);
		
		Label healthLabel = new Label();
		healthLabel.setText(String.format("%d", health));
		healthLabel.relocate(this.sizeX*0.88,this.sizeY*0.80);
		healthLabel.setTextFill(Color.WHITE);
		this.getChildren().add(healthLabel);
		
		Label manaCostLabel = new Label();
		manaCostLabel.setText(String.format("Cost = %d", manaCost));
		manaCostLabel.relocate(this.sizeX*0.50,this.sizeY*0.15);
		manaCostLabel.setTextFill(Color.WHITE);
		this.getChildren().add(manaCostLabel);
	}
	
	private void createCardActivateButton() {
		Button button = new Button();
		//button.setStyle("-fx-background-color:transparent");
		//button.minWidth(100);
		//button.minHeight(100);
		//button.prefWidth(100);
		//button.prefHeight(100);
		button.setOnAction(this::buttonClick);
		this.getChildren().add(button);
	}
	private void buttonClick(ActionEvent event) {
		System.out.println("Trying to Perform Action");
		List<ECSAction> cardActions = Actions.getPossibleActionsOn(card, performer);
		
		//If there is more than one action, create the choice box
		//Otherwise, the action will be automaticcally performed if there is only one
		if (cardActions.size() > 1) {
			this.controller.buildChoiceBoxPane(card, cardActions);
		} else if (cardActions.size() == 1) {
			for (ECSAction action : cardActions) {
				if (action.isAllowed(performer)) {
					if (!action.getTargetSets().isEmpty()) {
						TargetSet targetAction = action.getTargetSets().get(0);
						targetAction.clearTargets();
						List<Entity> targets = targetAction.findPossibleTargets();
						if (targets.isEmpty()) {
							return;
						}

						//int targetIndex = Integer.parseInt(input.nextLine());
						int targetIndex = 0;
						if (targetIndex < 0 || targetIndex >= cardActions.size()) {
							return;
						}
						
						//TODO: add a check to make sure the target is valid//
						Entity target = targets.get(targetIndex);
						targetAction.addTarget(target);
						action.perform(performer);
					} else {
						action.perform(performer);
						this.controller.createData();
						this.controller.render();
					}
				}
			}
		}
	}
	
	//TARGETING
	public void createTargetButton() {
		this.getChildren().clear();
		this.createCardBackground(true);
		this.createCardArt();
		this.createCardIDLabel();
		this.createCardPropertyLabelsGroup();
		this.createCardTargetButton();	 
	}
	private void createCardTargetButton() {
		Button button = new Button();
		button.setOnAction(this::targetButtonClick);
		this.getChildren().add(button);
	}
	private void targetButtonClick(ActionEvent event) {
		this.controller.performNextAction(this.card);
	}
}
