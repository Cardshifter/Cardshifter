package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class AttackTargetMinionsFirstThenPlayer extends SpecificActionTargetSystem {

	private final ResourceRetriever attackFirstResource;

	public AttackTargetMinionsFirstThenPlayer(ECSResource requiredResource) {
		super("Attack");
		this.attackFirstResource = ResourceRetriever.forResource(requiredResource);
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
			if (target.getComponent(BattlefieldComponent.class).stream().anyMatch(e -> attackFirstResource.getOrDefault(e, 0) > 0)) {
				event.setAllowed(false);
			}
		}
		else {
			event.setAllowed(false);
		}
	}

}
