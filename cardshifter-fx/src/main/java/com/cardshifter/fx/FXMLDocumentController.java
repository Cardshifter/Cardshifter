package com.cardshifter.fx;

import com.cardshifter.core.CommandLineOptions;
import com.cardshifter.core.Game;
import com.cardshifter.core.Player;
import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import com.cardshifter.core.Zone;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

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
        if (!gameHasStarted) {
            label.setText("Starting Game");
            game.getEvents().startGame(game);
            gameHasStarted = true;
        }
    }
    
    @FXML
    private Label turnLabel;
    
    @FXML
    private void handleTurnButtonAction(ActionEvent event) {
        if (gameHasStarted) {
            game.nextTurn();
            turnLabel.setText(String.format("Turn Number %d", game.getTurnNumber()));
        }
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
