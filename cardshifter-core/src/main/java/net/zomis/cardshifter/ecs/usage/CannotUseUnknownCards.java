package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.Retriever;
import net.zomis.cardshifter.ecs.cards.CardComponent;

public class CannotUseUnknownCards implements ECSSystem {

	@Retriever
	private ComponentRetriever<CardComponent> card;
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(ActionAllowedCheckEvent.class, this::denyUnknownCards);
	}
	
	private void denyUnknownCards(ActionAllowedCheckEvent event) {
		Entity actionEntity = event.getEntity();
		if (card.has(actionEntity)) {
			CardComponent cardData = card.get(actionEntity);
			cardData.getCurrentZone().isKnownTo(event.getPerformer());
		}
	}

}
