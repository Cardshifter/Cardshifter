package com.cardshifter.fx;

import org.apache.log4j.PropertyConfigurator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class just loads the FXML document which initializes its DocumentController
 */
public final class JavaFXGame extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
		PropertyConfigurator.configure(JavaFXGame.class.getResourceAsStream("log4j.properties"));
		stage.setTitle("Cardshifter");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
		Parent root = loader.load();
		FXMLGameController controller = loader.getController();
		stage.setOnCloseRequest(e -> controller.shutdown());
		
		Scene scene = new Scene(root);
		
		stage.setScene(scene);
		//stage.centerOnScreen();
		stage.show();
	}
	
	/**
	 * Main method
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
