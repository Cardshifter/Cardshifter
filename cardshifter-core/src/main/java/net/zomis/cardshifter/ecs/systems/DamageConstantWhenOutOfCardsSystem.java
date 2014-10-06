package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.cards.DrawCardFailedEvent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;

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
