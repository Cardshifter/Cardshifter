package com.cardshifter.client;

import javafx.scene.layout.Pane;

public class PlayerHandZoneView extends ZoneView<CardHandDocumentController> {
	
	public PlayerHandZoneView(int cardId, Pane pane) {
		super(cardId, pane);
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

	public void setCardTargetable(int target) {
		CardHandDocumentController card = getCard(target);
		card.setCardTargetable();
	}

	public void removeTargetableAllCards() {
		for (Integer cardId : this.getAllIds()) {
			CardHandDocumentController card = getCard(cardId);
			card.removeCardActive();
		}
	}
	
}
