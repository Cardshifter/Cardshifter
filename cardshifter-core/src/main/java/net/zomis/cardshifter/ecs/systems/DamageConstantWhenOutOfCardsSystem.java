package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.events.DrawCardFailedEvent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceRetreiver;

public class DamageConstantWhenOutOfCardsSystem implements System {

	private final int damage;
	private final ResourceRetreiver resource;

	public DamageConstantWhenOutOfCardsSystem(ECSResource resource, int damage) {
		this.damage = damage;
		this.resource = ResourceRetreiver.forResource(resource);
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(DrawCardFailedEvent.class, this::damage);
	}
	
	private void damage(DrawCardFailedEvent event) {
		resource.resFor(event.getEntity()).change(-damage);
	}

}
