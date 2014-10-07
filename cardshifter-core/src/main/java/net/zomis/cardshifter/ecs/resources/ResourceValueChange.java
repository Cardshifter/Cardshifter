package net.zomis.cardshifter.ecs.resources;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.events.IEvent;

public class ResourceValueChange implements IEvent {

	private final Entity entity;
	private final int previousValue;
	private final int previousActualValue;
	private final int newValue;
	private final ECSResource resource;

	public ResourceValueChange(Entity entity, ECSResource resource, int previousValue, int previousActualValue, int newValue) {
		this.entity = entity;
		this.resource = resource;
		this.previousValue = previousValue;
		this.previousActualValue = previousActualValue;
		this.newValue = newValue;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public int getNewValue() {
		return newValue;
	}
	
	public int getPreviousActualValue() {
		return previousActualValue;
	}
	
	public int getPreviousValue() {
		return previousValue;
	}
	
	public ECSResource getResource() {
		return resource;
	}
	
}
