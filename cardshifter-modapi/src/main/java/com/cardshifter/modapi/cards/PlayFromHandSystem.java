package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;

public class PlayFromHandSystem extends SpecificActionSystem {

	public PlayFromHandSystem(String name) {
		super(name);
	}

	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		if (!Cards.isOwnedByCurrentPlayer(event.getEntity())) {
			event.setAllowed(false);
		}
		if (!Cards.isOnZone(event.getEntity(), HandComponent.class)) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
	}

}
