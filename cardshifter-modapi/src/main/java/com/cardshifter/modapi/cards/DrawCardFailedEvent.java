package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class DrawCardFailedEvent implements IEvent {

	private final Entity entity;

	public DrawCardFailedEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
