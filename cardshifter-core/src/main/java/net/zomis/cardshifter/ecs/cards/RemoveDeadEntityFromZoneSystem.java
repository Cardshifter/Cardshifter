package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.EntityRemoveEvent;

public class RemoveDeadEntityFromZoneSystem implements ECSSystem {

	private final ComponentRetriever<CardComponent> zones = ComponentRetriever.retreiverFor(CardComponent.class);
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerBefore(EntityRemoveEvent.class, this::removeEntity);
	}

	private void removeEntity(EntityRemoveEvent event) {
		CardComponent card = zones.get(event.getEntity());
		if (card != null) {
			ZoneComponent currentZone = card.getCurrentZone();
			System.out.println("Removing entity " + event.getEntity() + " from zone " + currentZone);
			currentZone.cardMoveFrom(event.getEntity());
		}
	}
	
}
