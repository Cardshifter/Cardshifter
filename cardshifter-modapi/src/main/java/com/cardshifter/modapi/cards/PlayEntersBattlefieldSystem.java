package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;

public class PlayEntersBattlefieldSystem extends SpecificActionSystem {

	public PlayEntersBattlefieldSystem(String action) {
		super(action);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		CardComponent card = event.getEntity().getComponent(CardComponent.class);
		card.moveToBottom(card.getOwner().getComponent(BattlefieldComponent.class));
	}

}
