package com.cardshifter.client;

import javafx.scene.layout.Pane;

import com.cardshifter.api.outgoing.UseableActionMessage;

public class BattlefieldZoneView extends ZoneView<CardBattlefieldDocumentController> {
	
	public BattlefieldZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	public void setCardCanAttack(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = super.getCard(cardId);
		card.setCardAttackActive(message);
	}
	
	public void removeActiveAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardActive((int)cardId);
		}
	}
	
	private void removeCardActive(int cardId) {
		CardBattlefieldDocumentController card = super.getCard(cardId);
		if (card.isCardActive()) {
			card.removeCardActive();
		}
	}
	
	public void removeTargetableAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardTargetable((int)cardId);
		}
	}
	
	public void removeCardTargetable(int cardId) {
		CardBattlefieldDocumentController card = getCard(cardId);
		card.removeCardActive();
	}

	public void setCardTargetable(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = getCard(cardId);
		card.setCardTargetable(message);
	}
	
}