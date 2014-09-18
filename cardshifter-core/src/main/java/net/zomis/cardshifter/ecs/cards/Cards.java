package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.phase.PhaseController;

public class Cards {

	private static final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	private static final ComponentRetriever<PhaseController> phase = ComponentRetriever.singleton(PhaseController.class);
	
	public static boolean isOnZone(Entity entity, Class<? extends ZoneComponent> class1) {
		return card.required(entity).getCurrentZone().getClass().isAssignableFrom(class1);
	}

	public static boolean isOwnedByCurrentPlayer(Entity entity) {
		CardComponent cardData = card.required(entity);
		
		return cardData.getCurrentZone().getOwner() == phase.get(entity).getCurrentPhase().getOwner();
	}

	public static boolean isOwnedBy(Entity cardEntity, Entity player) {
		return card.required(cardEntity).getCurrentZone().getOwner() == player;
	}

}
