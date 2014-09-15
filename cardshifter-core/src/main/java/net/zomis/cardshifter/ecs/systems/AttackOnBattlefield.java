package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.components.BattlefieldComponent;
import net.zomis.cardshifter.ecs.events.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.events.ActionPerformEvent;

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
