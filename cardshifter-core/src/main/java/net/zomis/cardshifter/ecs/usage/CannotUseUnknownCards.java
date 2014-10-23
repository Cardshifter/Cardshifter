package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.cards.CardComponent;

public class CannotUseUnknownCards implements ECSSystem {

	@Retriever
	private ComponentRetriever<CardComponent> card;
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, this::denyUnknownCards);
	}
	
	private void denyUnknownCards(ActionAllowedCheckEvent event) {
		Entity actionEntity = event.getEntity();
		if (card.has(actionEntity)) {
			CardComponent cardData = card.get(actionEntity);
			cardData.getCurrentZone().isKnownTo(event.getPerformer());
		}
	}

}
