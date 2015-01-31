package com.cardshifter.client.views;

import javafx.scene.layout.Pane;

import com.cardshifter.api.outgoing.UsableActionMessage;

public class BattlefieldZoneView extends ZoneView<CardBattlefieldDocumentController> {
	
	public BattlefieldZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	public void setCardCanAttack(int cardId, UsableActionMessage message) {
		CardBattlefieldDocumentController card = super.getCard(cardId);
		card.setCardAttackActive(message);
	}
	
	public void setCardIsAttacking(int cardId) {
		CardBattlefieldDocumentController card = super.getCard(cardId);
		card.setCardIsAttacking();
	}
	
	public void setCardTargetable(int cardId, UsableActionMessage message) {
		CardBattlefieldDocumentController card = getCard(cardId);
		card.setCardTargetable();
	}
	
	@Override
	public void setCardScrappable(int cardId, UsableActionMessage message) {
		CardBattlefieldDocumentController card = getCard(cardId);
		card.setCardScrappable(message);
	}
	
}