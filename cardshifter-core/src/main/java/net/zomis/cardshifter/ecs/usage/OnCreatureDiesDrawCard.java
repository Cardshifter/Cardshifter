package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.events.EntityRemoveEvent;

/**
 * If a card on the battlefield dies, owner draws one card.
 * 
 * @author Simon Forsberg
 */
public class OnCreatureDiesDrawCard implements ECSSystem {

	/**
	 * Registers this system with EntityRemoveEvent.
	 * 
	 * @param game The game to register the system to.
	 */
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerBefore(this, EntityRemoveEvent.class, this::removed);
	}
	
	/**
	 * If the entity being removed has a CardComponent and is in a zone 
	 * with a BattlefieldComponent, the owner draws one card.
	 * 
	 * @param event The EntityRemoveEvent
	 */
	private void removed(EntityRemoveEvent event) {
		if (event.getEntity().hasComponent(CardComponent.class)) {
			if (Cards.isOnZone(event.getEntity(), BattlefieldComponent.class)) {
				Entity owner = event.getEntity().getComponent(CardComponent.class).getOwner();
				DrawStartCards.drawCard(owner);
			}
		}
	}

}
