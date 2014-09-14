package com.cardshifter.fx;

import com.cardshifter.core.Card;
import com.cardshifter.core.LuaTools;
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
    private final Card card;
    public CardHandDocumentController(Card card, FXMLGameController controller) {
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
        List<String> keyList = new ArrayList<>();
        LuaTools.processLuaTable(card.data.checktable(), (k, v) -> keyList.add(k + ""));
        for (String string : keyList) {
            if (string.equals("health")) {
                health.setText(String.format("%d", card.data.get("health").toint()));
            } else if (string.equals("strength")) {
                strength.setText(String.format("%d", card.data.get("strength").toint()));
            } else if (string.equals("manaCost")) {
                manaCost.setText(String.format("Mana Cost = %d", card.data.get("manaCost").toint()));
            } else if (string.equals("scrapCost")) {
                scrapCost.setText(String.format("Scrap Cost = %d", card.data.get("scrapCost").toint()));
            } else if (string.equals("cardType")) {
                cardType.setText(String.format(card.data.get("cardType").tojstring()));
            } else if (string.equals("creatureType")) {
                creatureType.setText(String.format(card.data.get("creatureType").tojstring()));
            } else if (string.equals("enchStrength")) {
                enchStrength.setText(String.format("Enchantment Strength = %d", card.data.get("enchStrength").toint()));
            } else if (string.equals("enchHealth")) {
                enchHealth.setText(String.format("Enchantment Health = %d", card.data.get("enchHealth").toint()));
            }
        }
    }

    //Boilerplate code
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
