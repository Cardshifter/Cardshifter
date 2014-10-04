package com.cardshifter.client;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UseableActionMessage;

import java.net.URL;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public final class CardBattlefieldDocumentController implements Initializable {
    
    @FXML private Label strength;
    @FXML private Label health;
    @FXML private Label cardId;
    @FXML private Label cardType;
    @FXML private Label creatureType;
	@FXML private Rectangle background;
	@FXML private Circle sicknessCircle;
	@FXML private AnchorPane anchorPane;
	@FXML private Button scrapButton;
    
//    private AnchorPane root;
	private boolean isActive;
    private final CardInfoMessage card;
	private final GameClientController controller;
	private UseableActionMessage message;
	
    public CardBattlefieldDocumentController(CardInfoMessage message, GameClientController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CardBattlefieldDocument.fxml"));
            loader.setController(this);
			loader.load();
//            root = loader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        } 
        this.card = message;
		this.controller = controller;
        this.setCardId();
        this.setCardLabels();
    }
	
	private void setCardId() {
        int newId = card.getId();
        cardId.setText(String.format("CardId = %d", newId));
    }
	
    private void setCardLabels() {
		for (Entry<String, Object> entry : this.card.getProperties().entrySet()) {
			Object value = entry.getValue();
			String stringValue = String.valueOf(entry.getValue());
			switch (entry.getKey()) {
				case "SICKNESS":
					if (value == (Integer) 1) {
						this.setSickness();
					}	
					break;
				case "ATTACK":
					strength.setText(stringValue);
					break;
				case "HEALTH":
					health.setText(stringValue);
					break;
				case "ATTACK_AVAILABLE":
					break;
				case "creatureType":
					creatureType.setText(stringValue);
					break;
			}
		}
    }
    
    public AnchorPane getRootPane() {
		return this.anchorPane;
    }
	
	public boolean isCardActive() {
		return this.isActive;
	}
	
	public void setCardAttackActive(UseableActionMessage message) {
		this.isActive = true;
		this.message = message;
		this.anchorPane.setOnMouseClicked(this::actionOnClick);
        background.setFill(Color.DARKGREEN);
		
		this.setUpScrapButton();
	}

    public void setCardActive(UseableActionMessage message) {
		this.isActive = true;
		this.message = message;
		this.anchorPane.setOnMouseClicked(this::actionOnClick);
        background.setFill(Color.YELLOW);
    }
	
	public void removeCardActive() {
		this.isActive = false;
		this.message = null;
		this.anchorPane.setOnMouseClicked(e -> {});
		background.setFill(Color.BLACK);
		this.scrapButton.setVisible(false);
	}
	
	public void setCardTargetable(UseableActionMessage message) {
		this.message = message;
		this.anchorPane.setOnMouseClicked(this::actionOnClick);
		background.setFill(Color.BLUE);
	}
	
	private void setSickness() {
		sicknessCircle.setVisible(true);
	}
	
	public void removeSickness() {
		sicknessCircle.setVisible(false);
	}
	
	private void setUpScrapButton() {
		scrapButton.setVisible(true);
		scrapButton.setOnMouseClicked(this::scrapButtonAction);
	}
	
	private void scrapButtonAction(MouseEvent event) {
		scrapButton.setVisible(false);
		UseableActionMessage scrapMessage = new UseableActionMessage(this.message.getId(), "Scrap", false, 0);
		this.controller.createAndSendMessage(scrapMessage);
	}
	
	private void actionOnClick(MouseEvent event) {
		this.controller.createAndSendMessage(this.message);
	}

	public void updateFields(UpdateMessage message) {
		if (message.getKey().equals("ATTACK")) {
			strength.setText(String.format("%d", message.getValue()));
		} else if (message.getKey().equals("HEALTH")) {
			health.setText(String.format("%d", message.getValue()));
		} else if (message.getKey().equals("creatureType")) {
			creatureType.setText(String.valueOf(message.getValue()));
		}
	}

    //Boilerplate code
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
