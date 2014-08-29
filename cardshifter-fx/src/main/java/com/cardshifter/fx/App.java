package com.cardshifter.fx;

import com.cardshifter.core.CommandLineOptions;
import com.cardshifter.core.Game;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    /*
    private final Game game;
    
    public App(final Game game) {
        this.game = Objects.requireNonNull(game, "game");;
    }
    */
    
    @Override
    public void start(Stage stage) throws Exception {     
        //stage.setTitle("title");
        
        this.startGame();
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        //stage.centerOnScreen();
        stage.show();
    }
    
     // @param args the command line arguments

    public static void main(String[] args) {
        launch(args);
    }
    
    public void startGame() throws Exception {
        CommandLineOptions options = new CommandLineOptions();
        InputStream file = options.getScript() == null ? Game.class.getResourceAsStream("start.lua") : new FileInputStream(new File(options.getScript()));
	Game game = new Game(file, options.getRandom());
	game.getEvents().startGame(game);
    }
    
}