package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.phase.PhaseController;

public class Cards {

	private static final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	private static final ComponentRetriever<PhaseController> phase = ComponentRetriever.singleton(PhaseController.class);
	
	public static boolean isOnZone(Entity entity, Class<? extends ZoneComponent> class1) {
		ZoneComponent zone = card.required(entity).getCurrentZone();
		return class1.isAssignableFrom(zone.getClass());
	}

	public static boolean isOwnedByCurrentPlayer(Entity entity) {
		CardComponent cardData = card.required(entity);
		
		return cardData.getCurrentZone().getOwner() == phase.get(entity).getCurrentPhase().getOwner();
	}

	public static boolean isOwnedBy(Entity cardEntity, Entity player) {
		return getOwner(cardEntity) == player;
	}

	public static Entity getOwner(Entity cardEntity) {
		return card.required(cardEntity).getOwner();
	}

	public static boolean isCard(Entity target) {
		return card.has(target);
	}

}
