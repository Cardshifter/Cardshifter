package com.cardshifter.client;

import com.cardshifter.server.outgoing.CardInfoMessage;
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

public class CardHandDocumentController implements Initializable {
    
    @FXML private Label strength;
    @FXML private Label health;
    @FXML private Label cardId;
    @FXML private Label manaCost;
    @FXML private Label scrapCost;
    @FXML private Label cardType;
    @FXML private Label creatureType;
    @FXML private Label enchStrength;
    @FXML private Label enchHealth;
	@FXML private Rectangle background;
	@FXML private AnchorPane anchorPane;
    
    //Initialization
    private AnchorPane root;
    private final CardInfoMessage card;
    public CardHandDocumentController(CardInfoMessage message, GameClientController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CardHandDocument.fxml"));
            loader.setController(this);
            root = loader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
                
        this.card = message;
        this.setCardId();
        this.setCardLabels();
		this.setActionForRootPane();
    }
	
	private void setActionForRootPane() {
		this.anchorPane.setOnMouseClicked(this::actionOnClick);
	}
	private void actionOnClick(MouseEvent event) {
		System.out.println("Action detected on card" + this.cardId.textProperty());
	}
    
    public AnchorPane getRootPane() {
		return this.anchorPane;
    }

    public void setRectangleColorActive() {
        background.setFill(Color.YELLOW);
    }

    private void setCardId() {
        int newId = card.getId();
        cardId.setText(String.format("CardId = %d", newId));
    }
	
    private void setCardLabels() {
		for(String key : this.card.getProperties().keySet()) {
			if (key.equals("SICKNESS")) {
				
			} else if (key.equals("MANA_COST")) {
				manaCost.setText(String.format("Mana Cost = %d", this.card.getProperties().get(key)));
			} else if (key.equals("ATTACK")) {
				strength.setText(this.card.getProperties().get(key).toString());
			} else if (key.equals("HEALTH")) {
				health.setText(this.card.getProperties().get(key).toString());
			} else if (key.equals("ATTACK_AVAILABLE")) {
				
			}
		}
		
		/*
        List<ECSResourceData> keyList = new ArrayList<>();
        Resources.processResources(card, data -> keyList.add(data));
        for (ECSResourceData string : keyList) {
        	ECSResource resource = string.getResource();
        	int value = string.get();
        	
            if (resource == PhrancisResources.HEALTH) {
                health.setText(String.valueOf(string.get()));
            } else if (resource == PhrancisResources.ATTACK) {
                strength.setText(String.valueOf(string.get()));
            } else if (resource == PhrancisResources.MANA_COST) {
                manaCost.setText(String.format("Mana Cost = %d", string.get()));
            } else if (resource == PhrancisResources.SCRAP_COST) {
                scrapCost.setText(String.format("Scrap Cost = %d", value));
            }
//            } else if (resource == PhrancisResources.st string.equals("enchStrength")) {
//                enchStrength.setText(String.format("Enchantment Strength = %d", card.data.get("enchStrength").toint()));
//            } else if (string.equals("enchHealth")) {
//                enchHealth.setText(String.format("Enchantment Health = %d", card.data.get("enchHealth").toint()));
//            }
        }
        
        if (card.hasComponent(CreatureTypeComponent.class)) {
        	creatureType.setText(card.getComponent(CreatureTypeComponent.class).getCreatureType());
        }
        
//      } else if (string.equals("cardType")) {
//      cardType.setText(String.format(card.data.get("cardType").tojstring()));
				
		*/
    }

    //Boilerplate code
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
