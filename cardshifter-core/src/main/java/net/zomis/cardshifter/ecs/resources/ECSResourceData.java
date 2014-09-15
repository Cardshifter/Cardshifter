package net.zomis.cardshifter.ecs.resources;

import java.util.Objects;

import net.zomis.cardshifter.ecs.base.Entity;


public class ECSResourceData {

	private int previousGet;
	private int current;
	private ECSResourceStrategy strategy;
	
	private final Entity entity;
	private final ECSResource resource;
	
	public ECSResourceData(Entity entity, ECSResource resource) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null. Make sure that ResourceMap is added to an entity.");
		this.resource = Objects.requireNonNull(resource, "Resource cannot be null.");
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

	public boolean has(int want) {
		return this.get() >= want;
	}
	
	@Override
	public String toString() {
		return String.valueOf(current);
	}
	
}
