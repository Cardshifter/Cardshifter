package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.components.HandComponent;
import net.zomis.cardshifter.ecs.events.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.events.ActionPerformEvent;

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
