package com.cardshifter.client;

import com.cardshifter.console.NetworkConsoleController;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GameClientLauncher implements Initializable {
	
	//Initialization
    private Pane root;
	private final String ipAddress;
	private final int port;
	public GameClientLauncher(String ipAddress, int port) {
        try {
			/*
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientDocument.fxml"));
            loader.setController(this);
            root = loader.load();
			*/
			Parent root = FXMLLoader.load(getClass().getResource("ClientDocument.fxml"));
		
			Scene scene = new Scene(root);
			
			Stage gameStage = new Stage();
		
			gameStage.setScene(scene);
			//stage.centerOnScreen();
			gameStage.show();
		}
        catch (Exception e) {
            throw new RuntimeException(e);
        }
		
		this.ipAddress = ipAddress;
		this.port = port;
		
		this.startGame();
    }
	
	private void startGame() {
		try {
			NetworkConsoleController control = new NetworkConsoleController(this.ipAddress, this.port);
			control.play(new Scanner(System.in));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
 	}
	
	//Boilerplate code
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
