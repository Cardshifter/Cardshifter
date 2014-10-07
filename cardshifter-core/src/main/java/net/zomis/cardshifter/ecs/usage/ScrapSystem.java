package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.Retriever;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.Cards;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;

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
		Entity cardOwner = card.get(event.getEntity()).getOwner();
		int scrapValue = resource.getFor(event.getEntity());
		resource.resFor(cardOwner).change(scrapValue);
		event.getEntity().destroy();
	}
	
}
