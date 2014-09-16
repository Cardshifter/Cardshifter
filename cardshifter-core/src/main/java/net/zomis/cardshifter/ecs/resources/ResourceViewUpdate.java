package net.zomis.cardshifter.ecs.resources;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

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
