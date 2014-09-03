package com.cardshifter.fx;

import com.cardshifter.core.CommandLineOptions;
import com.cardshifter.core.Game;
import com.cardshifter.core.Card;
import com.cardshifter.core.CardAction;
import com.cardshifter.core.LuaTools;
import com.cardshifter.core.Player;
import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import com.cardshifter.core.Zone;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;

import org.luaj.vm2.lib.jse.CoerceLuaToJava;

public class FXMLDocumentController implements Initializable {
    
    //INITIAL GAME SETUP
    //need a forward declaration so that this is  global to the class
    Game game;
    //hack to make the buttons work properly
    private boolean gameHasStarted = false;
    //I think this is a public constructor, this code initializes the Game
    public FXMLDocumentController() throws Exception {
        CommandLineOptions options = new CommandLineOptions();
        InputStream file = options.getScript() == null ? Game.class.getResourceAsStream("start.lua") : new FileInputStream(new File(options.getScript()));
        game = new Game(file, options.getRandom());
    }
    
    //START GAME BUTTON
    @FXML
    private Label startGameLabel;
    @FXML
    private void startGameButtonAction(ActionEvent event) {
        if (gameHasStarted == false) {
            startGameLabel.setText("Starting Game");
            game.getEvents().startGame(game);
            gameHasStarted = true;
            this.renderHands();
        }
    }
   
    //UPDATE LOOP
    private void render() {
        this.renderHands();
    }
    private void renderHands() {
        this.renderPlayerHand();
        this.renderOpponentHand();
    }
    
    //TODO: Convert this to mana totals for players, and only increment every play rotation
    //TURN LABEL
    @FXML
    private Label turnLabel;
    //ADVANCE TURNS
    @FXML
    private void handleTurnButtonAction(ActionEvent event) {
        if (gameHasStarted == true) {
            game.nextTurn();
            turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
            this.renderHands();
        }
    }
    
    //RENDER PLAYER 2 CARD BACKS
    @FXML
    Pane player02Pane;
    private void renderOpponentHand() {
        int cardCount = this.getOpponentCardCount();
        int currentCard = 0;
        while(currentCard < cardCount) {
            Group cardGroup = new Group();
            cardGroup.setTranslateX(currentCard * 130);
            player02Pane.getChildren().add(cardGroup);
            
            Rectangle cardBack = new Rectangle(0,0,125,145);
            cardBack.setFill(Color.AQUAMARINE);
            cardGroup.getChildren().add(cardBack);
            
            currentCard++;
        }
    }
    private int getOpponentCardCount() {
        Player player = game.getLastPlayer(); 
        Zone hand = (Zone)CoerceLuaToJava.coerce(player.data.get("hand"), Zone.class);
        List<Card> cardsInHand = hand.getCards();
        return cardsInHand.size();
    }
    
    //RENDER PLAYER ONE CARDS
    /*BIG TO DO: Make all dimensions relative to the Pane size or Screen size*/
    @FXML
    private Pane player01Pane;
    private void renderPlayerHand() {
        //First get the list of Card objects
        List<Card> cardsInHand = this.getCurrentPlayerHand();
        
        List<Card> cardsWithActions = new ArrayList<>();
        List<UsableAction> actions = game.getAllActions().stream().filter(action -> 
                action.isAllowed()).collect(Collectors.toList());
        for (UsableAction action : actions) {
            if (action instanceof CardAction) {
                Card card = ((CardAction)action).getCard();
                cardsWithActions.add(card);
            }
        }
        
        //Create a group for each card for positioning
        //Another group is needed for each element in the LuaTable
        int cardIndex = 0;
        for (Card card : cardsInHand) {
            System.out.println("found a card");
            
            //check if the card is active
            boolean isCardActive = false;
            for (Card cardWithAction : cardsWithActions) {
                if (card == cardWithAction) {
                    isCardActive = true;
                    //do a test to see if another break is needed
                    break;
                }
            }
            
            Group cardGroup = new Group();
            cardGroup.setId(String.format("player01card%d", cardIndex));
            cardGroup.setTranslateX(cardIndex * 165);
            player01Pane.getChildren().add(cardGroup);
            
            Rectangle activeBackground = new Rectangle(-10,-10,170,240);
            activeBackground.setFill(Color.BLACK);
            if(isCardActive == true) {
                activeBackground.setFill(Color.YELLOW);
            }
            cardGroup.getChildren().add(activeBackground);
            
            Rectangle cardBack = new Rectangle(0,0,150,220);
            cardBack.setFill(Color.FIREBRICK);
            cardGroup.getChildren().add(cardBack);

            Label cardIdLabel = new Label();
            cardIdLabel.setText(String.format("CardID = %d", card.getId()));
            cardIdLabel.setTextFill(Color.WHITE);
            cardGroup.getChildren().add(cardIdLabel);
           
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
            
            if (isCardActive == true) {
                //only make a button when the card is active
                Button button = new Button();
                //button.setStyle("-fx-background-color:transparent");
                button.minWidth(100);
                button.minHeight(100);
                button.prefWidth(100);
                button.prefHeight(100);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
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
					if (targetIndex < 0 || targetIndex >= actions.size()) {
						return;
					}
					
                                        //TODO: add a check to make sure the target is valid//
					Targetable target = targets.get(targetIndex);
					targetAction.perform(target);
				}
				else action.perform();
                            }
                        }
                    }
                });
                cardGroup.getChildren().add(button);
            }

            cardIndex++;
        }
    }
    private List<Card> getCurrentPlayerHand() {
        Player player = game.getFirstPlayer(); 
        Zone hand = (Zone)CoerceLuaToJava.coerce(player.data.get("hand"), Zone.class);
        List<Card> cardsInHand = new ArrayList<>();
        hand.getCards().forEach(card -> {
            cardsInHand.add(card);
        });
        return cardsInHand;
    }
    
    //BOILERPLATE
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }       
    
    //NOT YET USED
    @FXML
    private QuadCurve handGuide;
}
