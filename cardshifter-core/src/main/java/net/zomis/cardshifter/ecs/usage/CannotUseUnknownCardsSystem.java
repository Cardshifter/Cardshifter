package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.cards.CardComponent;

/**
 * If the card is not known to the entity performing the event,
 * 
 * @author Simon Forsberg
 */
public class CannotUseUnknownCardsSystem implements ECSSystem {

	@Retriever
	private ComponentRetriever<CardComponent> card;
	
	/**
	 * Registers this system with ActionAllowedCheckEvent.
	 * 
	 * @param game The game to register the system to.
	 */
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, this::denyUnknownCards);
	}
	
	private void denyUnknownCards(ActionAllowedCheckEvent event) {
		Entity actionEntity = event.getEntity();
		if (card.has(actionEntity)) {
			CardComponent cardData = card.get(actionEntity);
			boolean known = cardData.getCurrentZone().isKnownTo(event.getPerformer());
			if (!known) {
				event.setAllowed(false);
			}
		}
	}

}
