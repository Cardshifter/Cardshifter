package com.cardshifter.client.views;

import java.util.Map.Entry;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UseableActionMessage;
import com.cardshifter.client.GameClientController;

public final class CardHandDocumentController extends CardView {
    
    @FXML private Label strength;
    @FXML private Label health;
    @FXML private Label cardId;
    @FXML private Label manaCost;
    @FXML private Label scrapCost;
	@FXML private Label scrapValue;
    @FXML private Label creatureType;
	@FXML private Label abilityText;
	@FXML private Rectangle background;
	@FXML private AnchorPane anchorPane;
    
	private boolean isActive;
    private final CardInfoMessage card;
	private final GameClientController controller;
	private UseableActionMessage message;
	
	//private Map<String, Integer> cardValues = new HashMap<>();
	
    public CardHandDocumentController(CardInfoMessage message, GameClientController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CardHandDocument.fxml"));
            loader.setController(this);
			loader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
                
        this.card = message;
		this.controller = controller;
        this.setCardId(message.getId());
        this.setCardLabels();
    }
	
	private void setCardId(int id) {
        cardId.setText(String.format("CardId = %d", id));
	}

	public CardInfoMessage getCard() {
		return this.card;
	}
    
	@Override
    public AnchorPane getRootPane() {
		return this.anchorPane;
    }
	
	public boolean isCardActive() {
		return this.isActive;
	}

	@Override
    public void setCardActive(UseableActionMessage message) {
		this.isActive = true;
		this.message = message;
		this.anchorPane.setOnMouseClicked(this::actionOnClick);
        background.setFill(Color.YELLOW);
    }
	
	@Override
	public void removeCardActive() {
		this.isActive = false;
		this.message = null;
		this.anchorPane.setOnMouseClicked(e -> {});
		background.setFill(Color.BLACK);
	}
	
	private void actionOnClick(MouseEvent event) {
		System.out.println("Action detected on card" + this.cardId.textProperty());
		this.controller.createAndSendMessage(this.message);
		background.setFill(Color.BLACK);
	}

    private void setCardLabels() {
        this.abilityText.setText("");
		for (Entry<String, Object> entry : this.card.getProperties().entrySet()) {
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			
			//Use this in conjunction with updateFields once the values for cards in hand can be
			//modified by spells
			/*
			try {
				this.cardValues.put(key, Integer.parseInt(value));
			} catch (NumberFormatException e) {
				System.out.println("Not a number");
			}
			*/
			
			if (key.equals("MANA_COST")) {
				manaCost.setText(String.format("Mana Cost = %s", value));
			} else if (key.equals("ATTACK")) {
				strength.setText(value);
			} else if (key.equals("HEALTH")) {
				health.setText(value);
			} else if (key.equals("SCRAP_COST")) {
				scrapCost.setText(String.format("Scrap Cost = %s", value));
			} else if (key.equals("creatureType")) {
				creatureType.setText(value);
			} else if (key.equals("SCRAP")) {
				scrapValue.setText(String.format("Scrap val = %s", value));
			} else if (key.equals("effect")) {
				String truncatedString = value.substring(0, Math.min(value.length(), 14));
				abilityText.setText(truncatedString);
			} else if (key.equals("name")) {
				// TODO: This is a temporary fix until a real name label is added
				if (abilityText.getText().isEmpty()) {
					abilityText.setText(value);
				}
			}
		}
    }

	@Override
	public void updateFields(UpdateMessage message) {
	}
	
	@Override
	public void setCardScrappable(UseableActionMessage message) {
	}

	@Override
	public void setCardTargetable() {
		this.anchorPane.setOnMouseClicked(this::actionOnTarget);
		background.setFill(Color.BLUE);
	}
	
	@Override
	public void removeCardScrappable() {
	}
	
	private void actionOnTarget(MouseEvent event) {
		boolean isChosenTarget = controller.addTarget(card.getId());
		background.setFill(isChosenTarget ? Color.VIOLET : Color.BLUE);
	}

}
