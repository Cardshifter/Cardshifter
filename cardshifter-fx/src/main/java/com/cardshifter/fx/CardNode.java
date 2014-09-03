package com.cardshifter.fx;

import com.cardshifter.core.Card;
import com.cardshifter.core.LuaTools;
import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CardNode {
    
    private final double sizeX;
    private final double sizeY;
    private final String name;
    private final Card card;
    private final FXMLGameController controller;
    
    private final Group cardGroup;
    
    public CardNode(double sizeX, double sizeY, String name, Card card, FXMLGameController controller) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.name = name;
        this.card = card;
        this.controller = controller;
        this.cardGroup = new Group();
        this.createCard();
    }
    
    public Group getCardGroup() {
        return this.cardGroup;
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
        cardIdLabel.setTextFill(Color.WHITE);
        cardGroup.getChildren().add(cardIdLabel);
    }
    
    private void createCardPropertyLabelsGroup() {
        //This gets the text from Lua and makes labels for each property
        int stringIndex = 0;
        List<String> stringList = new ArrayList<>();
        LuaTools.processLuaTable(card.data.checktable(), (k, v) -> stringList.add(k + ": " + v));
        for (String string : stringList) {
            Group cardTextStrings = new Group();
            cardTextStrings.setTranslateY(25 + (stringIndex * 25));
            cardGroup.getChildren().add(cardTextStrings);
            
            Label cardStringLabel = new Label();
            cardStringLabel.setText(string);
            cardStringLabel.setTextFill(Color.WHITE);
            cardTextStrings.getChildren().add(cardStringLabel);
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
        List<UsableAction> cardActions = card.getActions().values().stream().filter(UsableAction::isAllowed).collect(Collectors.toList());
        for (UsableAction action : cardActions) {
            if (action.isAllowed()) {
                if (action instanceof TargetAction) {
                    TargetAction targetAction = (TargetAction) action;
                    List<Targetable> targets = targetAction.findTargets();
                    if (targets.isEmpty()) {
                        return;
                    }

                    //int targetIndex = Integer.parseInt(input.nextLine());
                    int targetIndex = 0;
                    if (targetIndex < 0 || targetIndex >= cardActions.size()) {
                        return;
                    }
                        
                    //TODO: add a check to make sure the target is valid//
                    Targetable target = targets.get(targetIndex);
                    targetAction.perform(target);
                }
                else action.perform();
            }
            this.controller.render();
        }
    }
    
    private boolean isCardActive() {
        List<UsableAction> cardActions = card.getActions().values().stream().filter(UsableAction::isAllowed).collect(Collectors.toList());
        return cardActions.size() > 0;
    }
}