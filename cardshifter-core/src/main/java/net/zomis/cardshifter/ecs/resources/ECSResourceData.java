package net.zomis.cardshifter.ecs.resources;

import net.zomis.cardshifter.ecs.base.Entity;


public class ECSResourceData {

	private int previousGet;
	private int current;
	private ECSResourceStrategy strategy;
	
	private final Entity entity;
	private final ECSResource resource;
	
	public ECSResourceData(Entity entity, ECSResource resource) {
		this.entity = entity;
		this.resource = resource;
	}
	
	public int get() {
		int result = strategy == null ? current : strategy.getResource(entity, current);
		if (previousGet != result) {
			// Execute an event for UIs to update their values, or for other entities to react
			entity.getGame().getEvents().executePostEvent(new ResourceViewUpdate(entity, resource, result));
		}
		previousGet = result;
		return result;
	}
	
	public void set(int value) {
		// Execute change event (for taking damage, gaining life, etc...).
		if (this.current != value) {
			entity.getGame().executeEvent(new ResourceValueChange(entity, get(), current, value),
					() -> this.current = value);
		}
	}
	
	public void change(int value) {
		set(current + value);
	}

	public void setStrategy(ECSResourceStrategy strategy) {
		this.strategy = strategy;
	}
	
	
	
}
