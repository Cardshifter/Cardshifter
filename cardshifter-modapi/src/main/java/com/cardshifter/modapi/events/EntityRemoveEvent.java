package com.cardshifter.modapi.events;

import com.cardshifter.modapi.base.Entity;

public class EntityRemoveEvent implements IEvent {

	private final Entity entity;
	private final Entity causeOfDeath;

	public EntityRemoveEvent(Entity entity) {
		this.entity = entity;
		this.causeOfDeath = null;
	}

	public EntityRemoveEvent(Entity entity, Entity causeOfDeath) {
		this.entity = entity;
		this.causeOfDeath = causeOfDeath;
	}
	
	public Entity getEntity() {
		return entity;
	}

	public Entity getCauseOfDeath() {
		return causeOfDeath;
	}
}
