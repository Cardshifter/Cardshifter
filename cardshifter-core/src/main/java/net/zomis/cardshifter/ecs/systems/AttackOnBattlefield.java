package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.Cards;

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
