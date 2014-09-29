package com.cardshifter.client;

import com.cardshifter.api.outgoing.UseableActionMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.scene.layout.Pane;

public class PlayerHandZoneView extends ZoneView {
	
	private final Map<Integer, CardHandDocumentController> cardMap = new HashMap<>();
	
	public PlayerHandZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	public void addCardController(int cardId, CardHandDocumentController controller) {
		this.cardMap.put(cardId, controller);
		super.addPane(cardId, controller.getRootPane());
	}
	
	public void removeCardController(int cardId) {
		super.removePane(cardId);
		this.cardMap.remove(cardId);
	}
	
	public CardHandDocumentController getCardHandController(int cardId) {
		return this.cardMap.get(cardId);
	}
	
	public void setCardActive(int cardId, UseableActionMessage message) {
		CardHandDocumentController card = this.cardMap.get(cardId);
		card.setCardActive(message);
	}
	
	public void removeActiveAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardActive((int)cardId);
		}
	}
	
	private void removeCardActive(int cardId) {
		CardHandDocumentController card = this.cardMap.get(cardId);
		if (card.isCardActive()) {
			card.removeCardActive();
		}
	}
	
	@Override
	public int getSize() {
		return this.cardMap.size();
	}
	
	@Override
	public Set getAllIds() {
		return this.cardMap.keySet();
	}
	
}
