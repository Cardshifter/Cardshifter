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
import java.util.ListIterator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
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
    
    @FXML
    private Label label;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        //System.out.println("You clicked me!");
        if (gameHasStarted == false) {
            label.setText("Starting Game");
            game.getEvents().startGame(game);
            gameHasStarted = true;
            this.renderHands();
        }
    }
    
    private void renderHands() {
        this.renderPlayerHand();
        this.renderOpponentHand();
    }
    
    @FXML
    private Label turnLabel;
    
    @FXML
    private void handleTurnButtonAction(ActionEvent event) {
        if (gameHasStarted == true) {
            game.nextTurn();
            turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
            this.renderHands();
        }
    }
    
    @FXML
    private QuadCurve handGuide;
    
    //NOT YET FINISHED
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
            List<String> stringList = new ArrayList<String>();
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

            cardIndex++;
        }
    }
    private List<Card> getCurrentPlayerHand() {
        Player player = game.getFirstPlayer(); 
        Zone hand = (Zone)CoerceLuaToJava.coerce(player.data.get("hand"), Zone.class);
        List<Card> cardsInHand = new ArrayList<Card>();
        hand.getCards().forEach(card -> {
            cardsInHand.add(card);
        });
        return cardsInHand;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
  
    public void play() {
        Scanner input = new Scanner(System.in);
        while (!game.isGameOver()) {
            outputGameState();
            List<UsableAction> actions = game.getAllActions().stream().filter(action -> action.isAllowed()).collect(Collectors.toList());
            outputList(actions);
			
            String in = input.nextLine();
            if (in.equals("exit")) {
                break;
            }
			
            handleActionInput(actions, in, input);
            }
	//print("--------------------------------------------");
	outputGameState();
	//print("Game over!");
    }
    
    private void handleActionInput(final List<UsableAction> actions, final String in, Scanner input) {
	Objects.requireNonNull(actions, "actions");
	Objects.requireNonNull(in, "in");
	//print("Choose an action:");
		
	try {
            int value = Integer.parseInt(in);
            if (value < 0 || value >= actions.size()) {
		//print("Action index out of range: " + value);
		return;
            }
			
            UsableAction action = actions.get(value);
            //print("Action " + action);
            if (action.isAllowed()) {
		if (action instanceof TargetAction) {
                    TargetAction targetAction = (TargetAction) action;
                    List<Targetable> targets = targetAction.findTargets();
                    if (targets.isEmpty()) {
			//print("No available targets for action");
			return;
                    }
					
                    outputList(targets);
                    //print("Enter target index:");
                    int targetIndex = Integer.parseInt(input.nextLine());
                    if (value < 0 || value >= actions.size()) {
                        //print("Target index out of range: " + value);
                        return;
                    }
					
                    Targetable target = targets.get(targetIndex);
                    targetAction.perform(target);
		}
		else action.perform();
		//print("Action performed");
            }
            else {
		//print("Action is not allowed");
            }
        }
	catch (NumberFormatException ex) {
            //print("Illegal action index: " + in);
	}
    }

    private void outputList(final List<?> actions) {
        Objects.requireNonNull(actions, "actions");
	//print("------------------");
	ListIterator<?> it = actions.listIterator();
	while (it.hasNext()) {
            //print(it.nextIndex() + ": " + it.next());
	}
    }
    
    private void outputGameState() {
	//print("------------------");
	//print(this.game);
	for (Player player : game.getPlayers()) {
            //print(player);
            //player.getActions().values().forEach(action -> print(4, "Action: " + action));
            //printLua(4, player.data); // TODO: Some LuaData should probably be hidden from other players, or even from self.
	}
		
	for (Zone zone : game.getZones()) {
            //print(zone);
            if (zone.isKnownToPlayer(game.getCurrentPlayer())) {
		zone.getCards().forEach(card -> {
                    //print(4, card);
                    //card.getActions().values().forEach(action -> print(8, "Action: " + action));
                    //printLua(8, card.data);
		});
            }
	}
    }

    
    
    
}
