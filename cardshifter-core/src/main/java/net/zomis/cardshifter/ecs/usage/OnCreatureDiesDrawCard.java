package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.Cards;
import net.zomis.cardshifter.ecs.cards.DrawStartCards;
import net.zomis.cardshifter.ecs.events.EntityRemoveEvent;

public class OnCreatureDiesDrawCard implements ECSSystem {

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerBefore(this, EntityRemoveEvent.class, this::removed);
	}
	
	private void removed(EntityRemoveEvent event) {
		if (event.getEntity().hasComponent(CardComponent.class)) {
			if (Cards.isOnZone(event.getEntity(), BattlefieldComponent.class)) {
				Entity owner = event.getEntity().getComponent(CardComponent.class).getOwner();
				DrawStartCards.drawCard(owner);
			}
		}
	}

}
