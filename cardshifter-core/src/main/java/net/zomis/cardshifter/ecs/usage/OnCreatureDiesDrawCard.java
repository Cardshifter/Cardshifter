package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.events.EntityRemoveEvent;

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
