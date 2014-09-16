package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class DrawCardFailedEvent implements IEvent {

	private final Entity entity;

	public DrawCardFailedEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
