package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.events.EntityRemoveEvent;

public class RemoveDeadEntityFromZoneSystem implements ECSSystem {

	private final ComponentRetriever<CardComponent> zones = ComponentRetriever.retreiverFor(CardComponent.class);
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerBefore(this, EntityRemoveEvent.class, this::removeEntity);
	}

	private void removeEntity(EntityRemoveEvent event) {
		CardComponent card = zones.get(event.getEntity());
		if (card != null) {
			ZoneComponent currentZone = card.getCurrentZone();
			currentZone.cardMoveFrom(event.getEntity());
		}
	}
	
}
