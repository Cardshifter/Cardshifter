package com.cardshifter.client.views;

import javafx.scene.layout.Pane;

public class PlayerHandZoneView extends ZoneView<CardHandDocumentController> {
	
	public PlayerHandZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	@Override
	public void setCardTargetable(int target) {
		CardHandDocumentController card = getCard(target);
		card.setCardTargetable();
	}

}
