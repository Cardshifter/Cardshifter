package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class DamageConstantWhenOutOfCardsSystem implements ECSSystem {

	private final int damage;
	private final ResourceRetriever resource;

	public DamageConstantWhenOutOfCardsSystem(ECSResource resource, int damage) {
		this.damage = damage;
		this.resource = ResourceRetriever.forResource(resource);
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, DrawCardFailedEvent.class, this::damage);
	}
	
	private void damage(DrawCardFailedEvent event) {
		resource.resFor(event.getEntity()).change(-damage);
	}

	@Override
	public String toString() {
		return "DamageConstantWhenOutOfCardsSystem [damage=" + damage
				+ ", resource=" + resource + "]";
	}

}
