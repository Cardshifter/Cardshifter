package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class ScrapSystem extends SpecificActionSystem {

	private final ResourceRetriever resource;

	public ScrapSystem(ECSResource resource) {
		super("Scrap");
		this.resource = ResourceRetriever.forResource(resource);
	}

	@Retriever private ComponentRetriever<CardComponent> card;
	
	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		if (!Cards.isOwnedByCurrentPlayer(event.getEntity())) {
			event.setAllowed(false);
		}
		if (!Cards.isOnZone(event.getEntity(), BattlefieldComponent.class)) {
			event.setAllowed(false);
		}
		if (resource.getOrDefault(event.getEntity(), 0) <= 0) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
		forceScrap(event.getEntity());
	}
	
	public void forceScrap(Entity entity) {
		Entity cardOwner = Cards.getOwner(entity);
		int scrapValue = resource.getOrDefault(entity, 0);
		resource.resFor(cardOwner).change(scrapValue);
		entity.destroy();
	}
	
}
