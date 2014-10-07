package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.Cards;

public class AttackOnBattlefield extends SpecificActionSystem {

	public AttackOnBattlefield() {
		super("Attack");
	}

	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		if (!Cards.isOwnedByCurrentPlayer(event.getEntity())) {
			event.setAllowed(false);
		}
		if (!Cards.isOnZone(event.getEntity(), BattlefieldComponent.class)) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
	}

}
