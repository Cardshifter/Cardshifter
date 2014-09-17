package com.cardshifter.fx;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.components.CreatureTypeComponent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceData;
import net.zomis.cardshifter.ecs.resources.Resources;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

public class CardHandDocumentController implements Initializable {
    
    //FXML Declarations
    @FXML
    private Label strength;
    @FXML
    private Label health;
    @FXML
    private Label cardId;
    @FXML
    private Label manaCost;
    @FXML
    private Label scrapCost;
    @FXML
    private Label cardType;
    @FXML
    private Label creatureType;
    @FXML
    private Label enchStrength;
    @FXML
    private Label enchHealth;
    
    //Initialization
    private Pane root;
    private final Entity card;
    public CardHandDocumentController(Entity card, FXMLGameController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CardHandDocument.fxml"));
            loader.setController(this);
            root = loader.load();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
                
        this.card = card;
        this.setCardId();
        this.setCardLabels();
    }
    
    public Pane getRootPane() {
        return this.root;
    }
    
    //Card Background
    @FXML
    private Rectangle background;
    
    public void setRectangleColorActive() {
        background.setFill(Color.YELLOW);
    }
    
    //Card Labels
    private void setCardId() {
        int newId = card.getId();
        cardId.setText(String.format("CardId = %d", newId));
    }
    private void setCardLabels() {
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
    }

    //Boilerplate code
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
