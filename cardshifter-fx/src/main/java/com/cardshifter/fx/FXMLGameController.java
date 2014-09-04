package com.cardshifter.fx;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;

import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.ai.CompleteIdiot;
import com.cardshifter.core.Card;
import com.cardshifter.core.Game;
import com.cardshifter.core.Player;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import com.cardshifter.core.Zone;
import com.cardshifter.core.console.CommandLineOptions;
import javafx.scene.Node;

public class FXMLGameController implements Initializable {
   
    //INITIAL GAME SETUP
    private final CardshifterAI opponent = new CompleteIdiot();
    //need a forward declaration so that this is  global to the class
    Game game;
    //hack to make the buttons work properly
    private boolean gameHasStarted = false;
    //I think this is a public constructor, this code initializes the Game
    @FXML
    Pane anchorPane;
    public FXMLGameController() throws Exception {
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
            turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
            this.renderHands();
        }
    }
   
    //TODO: Create a fixed time step and render in that
    //UPDATE LOOP
    public void render() {

        //this is a hack to make the buttons disappear when the choice is made
        //will not work with a fixed time step
        Node choiceBoxPane = anchorPane.lookup("#choiceBoxPane");
        anchorPane.getChildren().remove(choiceBoxPane);
        
        this.renderHands();
        this.renderBattlefields();
    }
    private void renderHands() {
        player02Pane.getChildren().clear();
        player01Pane.getChildren().clear();
        this.renderOpponentHand();
        this.renderPlayerHand();
    }
    
    //TODO: Convert this to mana totals for players
    //TODO: Play rotation needs to be changed so that it does not revolve around player 1
    //TURN LABEL
    @FXML
    private Label turnLabel;
    //ADVANCE TURNS
    @FXML
    private void handleTurnButtonAction(ActionEvent event) {
        if (gameHasStarted == true) {
            game.nextTurn();
            
            //This is the AI doing the turn action
            while (game.getCurrentPlayer() == game.getLastPlayer()) {
            	UsableAction action = opponent.getAction(game.getCurrentPlayer());
            	if (action == null) {
            		System.out.println("Warning: Opponent did not properly end turn");
            		break;
            	}
            	action.perform();
            }
            
            turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
            this.render();
        }
    }
    
    //RENDER PLAYER 2 CARD BACKS
    @FXML
    Pane player02Pane;
    private void renderOpponentHand() {
        //Opponent cards are rendered differently because the faces are not visible
        double paneHeight = player02Pane.getHeight();
        double paneWidth = player02Pane.getWidth();
        
        int numCards = this.getOpponentCardCount();
        int maxCards = Math.max(numCards, 8);
        double cardWidth = paneWidth / maxCards;
        
        int currentCard = 0;
        while(currentCard < numCards) {
            Group cardGroup = new Group();
            cardGroup.setTranslateX(currentCard * (cardWidth*1.05)); //1.05 so there is space between cards
            player02Pane.getChildren().add(cardGroup);
            
            Rectangle cardBack = new Rectangle(0,0,cardWidth,paneHeight);
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
    @FXML
    private Pane player01Pane;
    private void renderPlayerHand() {
        List<Card> cardsInHand = this.getCurrentPlayerHand();
        int numCards = cardsInHand.size();

        int cardIndex = 0;
        for (Card card : cardsInHand) {
            CardNode cardNode = new CardNode(player01Pane, numCards, "testName", card, this);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("card%d", card.getId()));
            cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
            player01Pane.getChildren().add(cardGroup);
            
            cardIndex++;
        }
    }
    private List<Card> getCurrentPlayerHand() {
        Player player = game.getFirstPlayer(); 
        Zone hand = (Zone)CoerceLuaToJava.coerce(player.data.get("hand"), Zone.class);
        return hand.getCards();
    }
    
    //RENDER BATTLEFIELD
    @FXML
    Pane player02Battlefield;
    @FXML
    Pane player01Battlefield;
    private void renderBattlefields() {
        player02Battlefield.getChildren().clear();
        player01Battlefield.getChildren().clear();
        this.renderOpponentBattlefield();
        this.renderPlayerBattlefield();
    }
    private void renderOpponentBattlefield() {
        List<Card> cardsInBattlefield = this.getBattlefield(game.getLastPlayer());
        int numCards = cardsInBattlefield.size();

        int cardIndex = 0;
        for (Card card : cardsInBattlefield) {
            CardNodeBattlefield cardNode = new CardNodeBattlefield(player02Battlefield, numCards, "testName", card, this, false);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("card%d", card.getId()));
            cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
            player02Battlefield.getChildren().add(cardGroup);
            
            cardIndex++;
        } 
    }
    private void renderPlayerBattlefield() {
        List<Card> cardsInBattlefield = this.getBattlefield(game.getFirstPlayer());
        int numCards = cardsInBattlefield.size();

        int cardIndex = 0;
        for (Card card : cardsInBattlefield) {
            CardNodeBattlefield cardNode = new CardNodeBattlefield(player01Battlefield, numCards, "testName", card, this, true);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("card%d", card.getId()));
            cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
            player01Battlefield.getChildren().add(cardGroup);
            
            cardIndex++;
        } 
    }
    private List<Card> getBattlefield(Player player) {
        Zone battlefield = (Zone)CoerceLuaToJava.coerce(player.data.get("battlefield"), Zone.class);
        return battlefield.getCards();
    }
    
    //CHOICE BOX PANE
    public void buildChoiceBoxPane(Card card, List<UsableAction> actionList) {
        Pane choiceBoxPane = new Pane();
        choiceBoxPane.setPrefHeight(367);
        choiceBoxPane.setPrefWidth(550);
        choiceBoxPane.setTranslateX(326);
        choiceBoxPane.setTranslateY(150);
        choiceBoxPane.setId("choiceBoxPane");
        
        choiceBoxPane.getChildren().clear();
        
        int numChoices  = actionList.size();
        double paneHeight = choiceBoxPane.getPrefHeight();
        double paneWidth = choiceBoxPane.getPrefWidth();
        double choiceBoxWidth = paneWidth / numChoices;
        
        int actionIndex = 0;
        for(UsableAction action : actionList) {
            ChoiceBoxNode choiceBox = new ChoiceBoxNode(choiceBoxWidth, paneHeight, "testName", action, this);
            Group choiceBoxGroup = choiceBox.getChoiceBoxGroup();
            choiceBoxGroup.setTranslateX(actionIndex * choiceBoxWidth);
            choiceBoxPane.getChildren().add(choiceBox.getChoiceBoxGroup());
            
            actionIndex++;
        }
        
        anchorPane.getChildren().add(choiceBoxPane);
    }
    
    //TARGETING
    public void markTargets(List<Card> targets) {
        List<Node> cardsInPlayerBattlefield = player01Battlefield.getChildren();
        List<Node> cardsInOpponentBattlefield = player02Battlefield.getChildren();
        for (Card target : targets) {
            for(Node node : cardsInPlayerBattlefield) {
                if(node.getId().equals(String.format("card%d",target.getId())) == true) {
                    CardNodeBattlefield actionNode = (CardNodeBattlefield)node;
                    actionNode.createTargetButton();
                }
            }
            for(Node node : cardsInOpponentBattlefield) {
                if(node.getId().equals(String.format("card%d",target.getId())) == true) {
                    CardNodeBattlefield actionNode = (CardNodeBattlefield)node;
                    actionNode.createTargetButton();
                }
            }
        }
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
