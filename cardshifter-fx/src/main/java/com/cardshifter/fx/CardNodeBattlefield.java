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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CardNodeBattlefield extends Group {
    
    private final double sizeX;
    private final double sizeY;
    private final String name;
    private final Card card;
    private final FXMLGameController controller;
    private final boolean isPlayer;
    
    public CardNodeBattlefield (Pane pane, int numCards, String name, Card card, FXMLGameController controller, boolean isPlayer) {
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
        this.isPlayer = isPlayer;
        this.createCard();
    }
    
    public Group getCardGroup() {
        return this;
    }
    
    public double getWidth() {
        return this.sizeX;
    }
    
    public Card getCard() {
        return this.card;
    }
    
    private void createCard() {
        this.createCardBackground(false);
        this.createCardArt();
        this.createCardIDLabel();
        //this.createCardPropertyLabelsGroup();
        
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
        if(this.isCardActive()) {
            activeBackground.setFill(Color.YELLOW);
        }
        
        if(targetMode) {
            activeBackground.setFill(Color.BLUE);
        }
        
        this.getChildren().add(activeBackground);
    }
    
    private void createCardArt() {
        Rectangle cardBack = new Rectangle(0,0,this.sizeX*0.95,this.sizeY*0.95);
        cardBack.setFill(Color.FIREBRICK);
        this.getChildren().add(cardBack);
    }
    
    private void createCardIDLabel() {
        Label cardIdLabel = new Label();
        cardIdLabel.setText(String.format("CardID = %d", card.getId()));
        cardIdLabel.setTextFill(Color.WHITE);
        this.getChildren().add(cardIdLabel);
    }
    
    private void createCardPropertyLabelsGroup() {
        //This gets the text from Lua and makes labels for each property
        int stringIndex = 0;
        List<String> stringList = new ArrayList<>();
        LuaTools.processLuaTable(card.data.checktable(), (k, v) -> stringList.add(k + ": " + v));
        for (String string : stringList) {
            Group cardTextStrings = new Group();
            cardTextStrings.setTranslateY(25 + (stringIndex * 25));
            this.getChildren().add(cardTextStrings);
            
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
        this.getChildren().add(button);
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

                        //int targetIndex = Integer.parseInt(input.nextLine());
                        int targetIndex = 0;
                        if (targetIndex < 0 || targetIndex >= cardActions.size()) {
                            return;
                        }
                        
                        //TODO: add a check to make sure the target is valid//
                        Targetable target = targets.get(targetIndex);
                        targetAction.setTarget(target);
                        targetAction.perform();
                    }
                    else action.perform();
                }
                this.controller.render();
            }
        }
    }
    
    private boolean isCardActive() {
        List<UsableAction> cardActions = card.getActions().values().stream().filter(UsableAction::isAllowed).collect(Collectors.toList());
        return cardActions.size() > 0;
    }
    
    //TARGETING
    public void createTargetButton() {
        this.getChildren().clear();
        this.createCardBackground(true);
        this.createCardArt();
        this.createCardIDLabel();
        this.createCardTargetButton();
    }
    private void createCardTargetButton() {
        Button button = new Button();
        button.setOnAction(this::targetButtonClick);
        this.getChildren().add(button);
    }
    private void targetButtonClick(ActionEvent event) {
        
    }
}