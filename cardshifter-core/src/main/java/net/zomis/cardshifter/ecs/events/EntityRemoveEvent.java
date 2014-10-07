package net.zomis.cardshifter.ecs.events;

import net.zomis.cardshifter.ecs.base.Entity;

public class EntityRemoveEvent implements IEvent {

	private final Entity entity;

	public EntityRemoveEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
