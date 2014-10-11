/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cardshifter.client.views;

import com.cardshifter.api.outgoing.CardInfoMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author baz
 */
public class DeckCardController {
	
		@FXML private AnchorPane anchorPane;
		@FXML private Label cardId;
		@FXML private Label cardDescription;
		@FXML private Label cardCount;
	
		private final CardInfoMessage card;
	
	    public DeckCardController(CardInfoMessage message, int count) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("DeckCardDocument.fxml"));
				loader.setController(this);
				loader.load();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
                
			this.card = message;
			this.setLabels(count);
		}
		
		private void setLabels(int count) {		
			this.cardCount.setText(String.format("%d", count));
			this.cardId.setText(String.format("%d", this.card.getId()));
			
			List<String> cardDescription = new ArrayList<>();
			for (Map.Entry<String, Object> entry : this.card.getProperties().entrySet()) {
				String key = entry.getKey();
				String value = String.valueOf(entry.getValue());
				if (key.equals("MANA_COST")) {
					cardDescription.add(String.format("Cost= %s", value));
				} else if (key.equals("ATTACK")) {
					cardDescription.add(String.format("A= %s", value));
				} else if (key.equals("HEALTH")) {
					cardDescription.add(String.format("H= %s", value));
				} else if (key.equals("SCRAP_COST")) {
					cardDescription.add(String.format("Scrap Cost= %s", value));
				} else if (key.equals("creatureType")) {
					cardDescription.add(String.format("Type= %s", value));
				} else if (key.equals("SCRAP")) {
					cardDescription.add(String.format("Val= %s", value));
				}
			}
			
			this.cardDescription.setText(cardDescription.toString());
		}
		
		public AnchorPane getRootPane() {
			return this.anchorPane;
		}
	
}
