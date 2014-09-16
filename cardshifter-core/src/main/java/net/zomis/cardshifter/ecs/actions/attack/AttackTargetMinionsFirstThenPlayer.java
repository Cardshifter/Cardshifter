package net.zomis.cardshifter.ecs.actions.attack;

import net.zomis.cardshifter.ecs.actions.TargetableCheckEvent;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.Cards;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.phase.PhaseController;

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
