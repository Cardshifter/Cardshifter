package com.cardshifter.client.views;

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
	
	public void setCardTargetable(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = getCard(cardId);
		card.setCardTargetable();
	}
	
	public void setCardScrappable(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = getCard(cardId);
		card.setCardScrappable(message);
	}
	
}