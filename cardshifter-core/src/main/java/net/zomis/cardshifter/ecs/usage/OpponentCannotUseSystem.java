package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.cards.CardComponent;

public class OpponentCannotUseSystem extends SpecificActionSystem {
	
	private final Entity entity;
	
	@Retriever private ComponentRetriever<CardComponent> card;
	@Retriever private ComponentRetriever<PlayerComponent> player;

	public OpponentCannotUseSystem(Entity whoAmI, String action) {
		super(action);
		this.entity = whoAmI;
	}
	
	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		final PlayerComponent me = findOwnerFor(entity);
		final PlayerComponent performer = findOwnerFor(event.getEntity());
		if (me != performer) {
			event.setAllowed(false);
		}
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
	}
	
	private PlayerComponent findOwnerFor(Entity entity) {
		if (player.has(entity)) {
			return player.get(entity);
		}
		else if (card.has(entity)) {
			return player.get(card.get(entity).getOwner());
		}
		else {
			throw new IllegalStateException(entity + " is not a player or a card. No idea who the player is.");
		}
	}

}
