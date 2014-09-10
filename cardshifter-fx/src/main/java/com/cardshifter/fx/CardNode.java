package com.cardshifter.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.cardshifter.core.Card;
import com.cardshifter.core.LuaTools;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.actions.TargetAction;
import com.cardshifter.core.actions.UsableAction;

/* 
   The purpose of this class is to take in certain values from the 
   Game controller and create a Group that the controller can 
   retrieve in order to render a Player card on the screen
*/

public class CardNode {
    
    private final double sizeX;
    private final double sizeY;
    private final String name;
    private final Card card;
    private final FXMLGameController controller;
    
    private final Group cardGroup;
    
    public CardNode(Pane pane, int numCards, String name, Card card, FXMLGameController controller) {
        //calculate card width based on pane size
        double paneWidth = pane.getWidth();
        //reduce card size if there are over a certain amount of them
        int maxCards = Math.max(numCards, 8);
        double cardWidth = paneWidth / maxCards;
        
        this.sizeX = cardWidth;
        this.sizeY = pane.getHeight();
        this.name = name;
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
        int labelSpacing = 20;
        List<String> stringList = new ArrayList<>();
        LuaTools.processLuaTable(card.data.checktable(), (k, v) -> stringList.add(k + ": " + v));
        for (String string : stringList) {           
            Label cardStringLabel = new Label();
            cardStringLabel.setText(string);
            cardStringLabel.setScaleY(0.90);
            cardStringLabel.setScaleX(0.90);
            cardStringLabel.relocate(0, labelSpacing + (stringIndex * labelSpacing));
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
        
        //these do not work (cannot find symbol)
        //button.width(100);
        //button.height(100);
        
        button.setOnAction(this::buttonClick);
        cardGroup.getChildren().add(button);
    }
    
    private void buttonClick(ActionEvent event) {
        System.out.println("Trying to Perform Action");
        List<UsableAction> cardActions = card.getActions().values().stream().filter(UsableAction::isAllowed).collect(Collectors.toList());
        
        //If there is more than one action, create the choice box
        if(cardActions.size() > 1) {
            this.controller.buildChoiceBoxPane(card, cardActions);
        } else if (cardActions.size() == 1) {
            for (UsableAction action : cardActions) {
                if (action.isAllowed()) {
                    if (action instanceof TargetAction) {
                        TargetAction targetAction = (TargetAction) action;
                        List<Targetable> targets = targetAction.findTargets();
                        if (targets.isEmpty()) {
                            return;
                        }

                        List<Card>targetCards = new ArrayList<>();
                        for(Targetable target : targets) {
                            if (target instanceof Card) {
                                targetCards.add((Card)target);
                            }
                        }
                        this.controller.markTargets(targetCards);
                        this.controller.nextAction = targetAction;
                    } else {
                        action.perform();
                        this.controller.createData();
                    } 
                }
                this.controller.render();
            }
        }
    }
    
    private boolean isCardActive() {
        List<UsableAction> cardActions = card.getActions().values().stream().filter(UsableAction::isAllowed).collect(Collectors.toList());
        return cardActions.size() > 0;
    }
}