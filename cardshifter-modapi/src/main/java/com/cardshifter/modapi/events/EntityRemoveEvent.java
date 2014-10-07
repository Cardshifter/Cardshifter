package com.cardshifter.modapi.events;

import com.cardshifter.modapi.base.Entity;

public class EntityRemoveEvent implements IEvent {

	private final Entity entity;

	public EntityRemoveEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
