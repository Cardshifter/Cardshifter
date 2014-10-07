package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class ResourceViewUpdate implements IEvent {

	private final Entity entity;
	private final ECSResource resource;
	private final int newValue;

	public ResourceViewUpdate(Entity entity, ECSResource resource, int newValue) {
		this.entity = entity;
		this.resource = resource;
		this.newValue = newValue;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public int getNewValue() {
		return newValue;
	}
	
	public ECSResource getResource() {
		return resource;
	}
	
}
