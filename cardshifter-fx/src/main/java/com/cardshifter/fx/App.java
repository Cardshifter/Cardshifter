package com.cardshifter.fx;

//import com.cardshifter.core.Game;
import com.cardshifter.core.ConsoleController;

//import java.util.Objects;

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
    
}