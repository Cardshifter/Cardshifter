package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.phase.PhaseController;

public class AttackTargetMinionsFirstThenPlayer extends SpecificActionTargetSystem {

	public AttackTargetMinionsFirstThenPlayer() {
		super("Attack");
	}
	
	protected void checkTargetable(TargetableCheckEvent event) {
		Entity target = event.getTarget();
		if (target.hasComponent(CardComponent.class)) {
			if (Cards.isOwnedByCurrentPlayer(target)) {
				event.setAllowed(false);
			}
			if (!Cards.isOnZone(target, BattlefieldComponent.class)) {
				event.setAllowed(false);
			}
		}
		else if (target.hasComponent(PlayerComponent.class)) {
			if (target == ComponentRetriever.singleton(target.getGame(), PhaseController.class).getCurrentEntity()) {
				event.setAllowed(false);
			}
			if (!target.getComponent(BattlefieldComponent.class).isEmpty()) {
				event.setAllowed(false);
			}
		}
		else {
			event.setAllowed(false);
		}
	}

}
