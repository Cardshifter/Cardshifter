package com.cardshifter.client;

import org.apache.log4j.PropertyConfigurator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class just loads the FXML document which initializes its DocumentController
 */
public final class GameClient extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {	  
		PropertyConfigurator.configure(getClass().getResourceAsStream("log4j.properties"));
		Parent root = FXMLLoader.load(getClass().getResource("LauncherDocument.fxml"));
		
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