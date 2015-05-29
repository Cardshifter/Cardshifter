package com.cardshifter.modapi.events;

import com.cardshifter.modapi.base.Entity;

public class EntityCreatedEvent implements IEvent {

	private final Entity entity;

	public EntityCreatedEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
