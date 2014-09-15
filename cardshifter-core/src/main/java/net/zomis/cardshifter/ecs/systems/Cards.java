package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.components.CardComponent;
import net.zomis.cardshifter.ecs.components.PhaseController;
import net.zomis.cardshifter.ecs.components.ZoneComponent;

public class Cards {

	private static final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	private static final ComponentRetriever<PhaseController> phase = ComponentRetriever.singleton(PhaseController.class);
	
	public static boolean isOnZone(Entity entity, Class<? extends ZoneComponent> class1) {
		return card.get(entity).getCurrentZone().getClass().isAssignableFrom(class1);
	}

	public static boolean isOwnedByCurrentPlayer(Entity entity) {
		return card.get(entity).getCurrentZone().getOwner() == phase.get(entity).getCurrentPhase().getOwner();
	}

}
