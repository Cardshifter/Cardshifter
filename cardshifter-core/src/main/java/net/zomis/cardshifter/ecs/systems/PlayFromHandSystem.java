package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.cards.Cards;
import net.zomis.cardshifter.ecs.cards.HandComponent;

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
