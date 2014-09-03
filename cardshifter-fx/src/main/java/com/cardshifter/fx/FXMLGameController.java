package com.cardshifter.fx;

import com.cardshifter.core.CommandLineOptions;
import com.cardshifter.core.Game;
import com.cardshifter.core.Card;
import com.cardshifter.core.Player;
import com.cardshifter.core.Zone;

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

public class FXMLGameController implements Initializable {
    
    //INITIAL GAME SETUP
    //need a forward declaration so that this is  global to the class
    Game game;
    //hack to make the buttons work properly
    private boolean gameHasStarted = false;
    //I think this is a public constructor, this code initializes the Game
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
            this.renderHands();
        }
    }
   
    //UPDATE LOOP
    public void render() {
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
        player02Pane.getChildren().clear();
        
        int numCards = this.getOpponentCardCount();
        double paneHeight = player02Pane.getHeight();
        double paneWidth = player02Pane.getWidth();
        double cardWidth = paneWidth / numCards;
        
        int currentCard = 0;
        while(currentCard < numCards) {
            Group cardGroup = new Group();
            cardGroup.setTranslateX(currentCard * (cardWidth*1.05));
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
        player01Pane.getChildren().clear();
                
        List<Card> cardsInHand = this.getCurrentPlayerHand();
        
        int numCards = cardsInHand.size();
        double paneHeight = player01Pane.getHeight();
        double paneWidth = player01Pane.getWidth();
        double cardWidth = paneWidth / numCards;

        int cardIndex = 0;
        for (Card card : cardsInHand) {
            CardNode cardNode = new CardNode(cardWidth, paneHeight, "testName", card, this);
            Group cardGroup = cardNode.getCardGroup();
            cardGroup.setAutoSizeChildren(true); //NEW
            cardGroup.setId(String.format("player01card%d", cardIndex));
            cardGroup.setTranslateX(cardIndex * cardWidth);
            player01Pane.getChildren().add(cardGroup);
            
            cardIndex++;
        }
    }
    private List<Card> getCurrentPlayerHand() {
        Player player = game.getFirstPlayer(); 
        Zone hand = (Zone)CoerceLuaToJava.coerce(player.data.get("hand"), Zone.class);
        return hand.getCards();
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
