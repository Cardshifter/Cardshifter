package com.cardshifter.modapi.players;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.cards.CardComponent;

public class Players {
	private static final ComponentRetriever<CardComponent> card = Retrievers.component(CardComponent.class);
	private static final ComponentRetriever<PlayerComponent> player = Retrievers.component(PlayerComponent.class);
	
	public static Entity findOwnerFor(Entity entity) {
		if (player.has(entity)) {
			return entity;
		}
		if (card.has(entity)) {
			return card.get(entity).getOwner();
		}
		return null;
	}

}
