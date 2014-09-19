package com.cardshifter.client;

import com.cardshifter.server.outgoing.CardInfoMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CardBattlefieldDocumentController implements Initializable {
    
    @FXML private Label strength;
    @FXML private Label health;
    @FXML private Label cardId;
    @FXML private Label cardType;
    @FXML private Label creatureType;
	@FXML private Rectangle background;
	@FXML private AnchorPane anchorPane;
    
    private AnchorPane root;
    private final CardInfoMessage card;
	private final GameClientController controller;
	private UseableActionMessage message;
	
    public CardBattlefieldDocumentController(CardInfoMessage message, GameClientController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CardBattlefieldDocument.fxml"));
            loader.setController(this);
            root = loader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
                
        this.card = message;
		this.controller = controller;
        this.setCardId();
        this.setCardLabels();
    }
    
    public AnchorPane getRootPane() {
		return this.anchorPane;
    }

    public void setCardActive(UseableActionMessage message) {
		this.message = message;
		this.anchorPane.setOnMouseClicked(this::actionOnClick);
        background.setFill(Color.YELLOW);
    }
	
	private void actionOnClick(MouseEvent event) {
		System.out.println("Action detected on card" + this.cardId.textProperty());
		this.controller.createAndSendMessage(this.message);
		background.setFill(Color.BLACK);
	}

    private void setCardId() {
        int newId = card.getId();
        cardId.setText(String.format("CardId = %d", newId));
    }
	
    private void setCardLabels() {
		for(String key : this.card.getProperties().keySet()) {
			if (key.equals("SICKNESS")) {
				
			} else if (key.equals("ATTACK")) {
				strength.setText(this.card.getProperties().get(key).toString());
			} else if (key.equals("HEALTH")) {
				health.setText(this.card.getProperties().get(key).toString());
			} else if (key.equals("ATTACK_AVAILABLE")) {
				
			}
		}
    }

    //Boilerplate code
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
