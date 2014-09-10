package com.cardshifter.fx;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
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
import com.cardshifter.core.Zone;
import com.cardshifter.core.actions.TargetAction;
import com.cardshifter.core.actions.UsableAction;
import com.cardshifter.core.console.CommandLineOptions;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class FXMLGameController implements Initializable {
   
    //INITIAL GAME SETUP
    private final CardshifterAI opponent = new CompleteIdiot();
    //need a forward declaration so that this is  global to the class
    private Game game;
    //hack to make the buttons work properly
    private boolean gameHasStarted = false;
    //I think this is a public constructor, this code initializes the Game
    @FXML
    Pane anchorPane;
    public FXMLGameController() throws Exception {
        this.initializeGame();
    }
    private void initializeGame() throws Exception {
        CommandLineOptions options = new CommandLineOptions();
        InputStream file = options.getScript() == null ? Game.class.getResourceAsStream("start.lua") : new FileInputStream(new File(options.getScript()));
        game = new Game(file, options.getRandom());
    }
    //GAME START and NEW GAME
    @FXML
    private Label startGameLabel;
    @FXML
    private void startGameButtonAction(ActionEvent event) {
        if (gameHasStarted == false) {
            startGameLabel.setText("Starting Game");
            game.getEvents().startGame(game);
            gameHasStarted = true;
            turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
            
            this.playerBattlefieldData = new ArrayList<>();
            this.opponentBattlefieldData = new ArrayList<>();
            this.playerHandData = new ArrayList<>();
            this.opponentHandData = new ArrayList<>();
            
            this.createData();
            this.render();
        }
    }
    @FXML
    private void newGameButtonAction(ActionEvent event) throws Exception {
        this.initializeGame();
        
        startGameLabel.setText("Starting Game");
        game.getEvents().startGame(game);
        gameHasStarted = true;
        turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
            
        this.playerBattlefieldData = new ArrayList<>();
        this.opponentBattlefieldData = new ArrayList<>();
        this.playerHandData = new ArrayList<>();
        this.opponentHandData = new ArrayList<>();
            
        this.createData();
        this.render();
    }

    //TODO: Create a fixed time step and render in that
    //RENDER - eventually change this to an Update method
    public void render() {
        //this is a hack to make the buttons disappear when the choice is made
        //will not work with a fixed time step
        Node choiceBoxPane = anchorPane.lookup("#choiceBoxPane");
        anchorPane.getChildren().remove(choiceBoxPane);
        
        this.renderHands();
        this.renderBattlefields();
        
        this.updateGameLabels();
    }
    
    //RENDER HANDS
    @FXML
    private Pane opponentHandPane;
    @FXML
    private Pane playerHandPane;
    private void renderHands() {
        this.renderOpponentHand();
        this.renderPlayerHand();
    }
    private void renderOpponentHand() {
        opponentHandPane.getChildren().clear();
        opponentHandPane.getChildren().addAll(opponentHandData);
    }
    private void renderPlayerHand() {
        playerHandPane.getChildren().clear();
        playerHandPane.getChildren().addAll(playerHandData);
    }
    
    //RENDER BATTLEFIELDS
    @FXML
    Pane opponentBattlefieldPane;
    @FXML
    Pane playerBattlefieldPane;
    private void renderBattlefields() {
        this.renderOpponentBattlefield();
        this.renderPlayerBattlefield();
    }
    private void renderOpponentBattlefield() {
        opponentBattlefieldPane.getChildren().clear();
        opponentBattlefieldPane.getChildren().addAll(opponentBattlefieldData);
    }
    private void renderPlayerBattlefield() {
        playerBattlefieldPane.getChildren().clear();
        playerBattlefieldPane.getChildren().addAll(playerBattlefieldData);
    }
    
    //Called at the end of turn, and when a card is played
    public void createData() {
        this.createHands();
        this.createBattlefields();
    }
    
    //CREATE HANDS
    private void createHands() {
        this.createOpponentHand();
        this.createPlayerHand();
    }
    //CREATE OPPONENT CARD BACKS
    private List<Group> opponentHandData;
    private void createOpponentHand() {
        opponentHandData.clear();
        
        //Opponent cards are rendered differently because the faces are not visible
        double paneHeight = opponentHandPane.getHeight();
        double paneWidth = opponentHandPane.getWidth();
        
        int numCards = this.getOpponentCardCount();
        int maxCards = Math.max(numCards, 8);
        double cardWidth = paneWidth / maxCards;
        
        int currentCard = 0;
        while(currentCard < numCards) {
            Group cardGroup = new Group();
            cardGroup.setTranslateX(currentCard * (cardWidth*1.05)); //1.05 so there is space between cards
            opponentHandData.add(cardGroup);
            
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
    //CREATE PLAYER HAND
    private List<Group> playerHandData;
    private void createPlayerHand() {
        playerHandData.clear();
        
        List<Card> cardsInHand = this.getCurrentPlayerHand();
        int numCards = cardsInHand.size();

        int cardIndex = 0;
        for (Card card : cardsInHand) {
            
            //uncomment these two lines and comment out all the lines below to enable this
            //also change the playerHandData list to contain Pane
            //CardHandDocumentController cardHand = new CardHandDocumentController(card, this);
            //playerHandData.add(cardHand.getRootPane());
            
            
            
            CardNode cardNode = new CardNode(playerHandPane, numCards, "testName", card, this);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("card%d", card.getId()));
            cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
            playerHandData.add(cardGroup);
            
            cardIndex++;
            
        }
    }
    private List<Card> getCurrentPlayerHand() {
        Player player = game.getFirstPlayer(); 
        Zone hand = (Zone)CoerceLuaToJava.coerce(player.data.get("hand"), Zone.class);
        return hand.getCards();
    }
    
    //CREATE BATTLEFIELDS
    private void createBattlefields() {
        this.createOpponentBattlefield();
        this.createPlayerBattlefield();
    }
    //CREATE OPPONENT BATTLEFIELD
    private List<Group> opponentBattlefieldData;
    private void createOpponentBattlefield() {
        opponentBattlefieldData.clear();
        
        List<Card> cardsInBattlefield = this.getBattlefield(game.getLastPlayer());
        int numCards = cardsInBattlefield.size();

        int cardIndex = 0;
        for (Card card : cardsInBattlefield) {
            CardNodeBattlefield cardNode = new CardNodeBattlefield(opponentBattlefieldPane, numCards, "testName", card, this, false);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("card%d", card.getId()));
            cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
            opponentBattlefieldData.add(cardGroup);
            
            cardIndex++;
        } 
    }
    //CREATE PLAYER BATTLEFIELD
    private List<Group> playerBattlefieldData;
    private void createPlayerBattlefield() {
        playerBattlefieldData.clear();
        
        List<Card> cardsInBattlefield = this.getBattlefield(game.getFirstPlayer());
        int numCards = cardsInBattlefield.size();

        int cardIndex = 0;
        for (Card card : cardsInBattlefield) {
            CardNodeBattlefield cardNode = new CardNodeBattlefield(playerBattlefieldPane, numCards, "testName", card, this, true);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("card%d", card.getId()));
            cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
            playerBattlefieldData.add(cardGroup);
            
            cardIndex++;
        } 
    }
    private List<Card> getBattlefield(Player player) {
        Zone battlefield = (Zone)CoerceLuaToJava.coerce(player.data.get("battlefield"), Zone.class);
        return battlefield.getCards();
    }
    
    //TODO: Convert this to mana totals for players
    //TODO: Play rotation needs to be changed so that it does not revolve around player 1
    //END TURN LABEL
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
            
            //reload data at the start of a new turn
            this.createData();
            this.render();
        }
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
    public TargetAction nextAction;
    public void performNextAction(Targetable target) {
        nextAction.setTarget(target);
        nextAction.perform();
        this.createData();
        this.render();
    }
    public void markTargets(List<Card> targets) {
        List<Node> cardsInPlayerBattlefield = playerBattlefieldPane.getChildren();
        List<Node> cardsInOpponentBattlefield = opponentBattlefieldPane.getChildren();
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
    
    //GAME STATE LABELS
    @FXML
    Label opponentLife;
    @FXML
    Label opponentCurrentMana;
    @FXML
    Label opponentTotalMana;
    @FXML
    Label opponentScrap;
    @FXML
    Label playerLife;
    @FXML
    Label playerCurrentMana;
    @FXML
    Label playerTotalMana;
    @FXML
    Label playerScrap;
    private void updateGameLabels() {
        Player localOpponent = game.getLastPlayer();
        opponentLife.setText(String.format("%d",localOpponent.data.get("life").toint()));
        opponentCurrentMana.setText(String.format("%d",localOpponent.data.get("mana").toint()));
        opponentTotalMana.setText(String.format("%d",localOpponent.data.get("manaMax").toint()));
        opponentScrap.setText(String.format("%d",localOpponent.data.get("scrap").toint()));
        
        Player player = game.getFirstPlayer();
        playerLife.setText(String.format("%d", player.data.get("life").toint()));
        playerCurrentMana.setText(String.format("%d", player.data.get("mana").toint()));
        playerTotalMana.setText(String.format("%d", player.data.get("manaMax").toint()));
        playerScrap.setText(String.format("%d", player.data.get("scrap").toint()));
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
