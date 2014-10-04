package com.cardshifter.client;

import javafx.scene.layout.Pane;

import com.cardshifter.api.outgoing.UseableActionMessage;

public class PlayerHandZoneView extends ZoneView<CardHandDocumentController> {
	
	public PlayerHandZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	public void setCardActive(int cardId, UseableActionMessage message) {
		CardHandDocumentController card = super.getCard(cardId);
		card.setCardActive(message);
	}
	
	public void removeActiveAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardActive((int)cardId);
		}
	}
	
	private void removeCardActive(int cardId) {
		CardHandDocumentController card = super.getCard(cardId);
		if (card.isCardActive()) {
			card.removeCardActive();
		}
	}
	
}
