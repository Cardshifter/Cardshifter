package com.cardshifter.client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

public class GameClientController implements Initializable {
	
	//Initialization
    private Pane root;
	public GameClientController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientDocument.fxml"));
            loader.setController(this);
            root = loader.load();
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
