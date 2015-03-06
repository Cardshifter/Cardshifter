package com.cardshifter.modapi.players;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.cards.CardComponent;

import java.util.*;

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

    public static Entity getNextPlayer(Entity entity) {
        List<Entity> players = new ArrayList<>(entity.getGame().getEntitiesWithComponent(PlayerComponent.class));
        players.sort(Comparator.comparingInt(pl -> player.get(pl).getIndex()));
        int myIndex = player.get(findOwnerFor(entity)).getIndex();
        Optional<Entity> result = players.stream().filter(pl -> player.get(pl).getIndex() == myIndex + 1).findFirst();
        return result.orElse(players.stream().filter(pl -> player.get(pl).getIndex() == 0).findFirst().get());
    }
}
