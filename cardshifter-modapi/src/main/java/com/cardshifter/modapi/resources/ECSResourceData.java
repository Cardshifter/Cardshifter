package com.cardshifter.modapi.resources;

import java.util.Objects;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.modapi.base.Entity;



public class ECSResourceData {

	private static final Logger logger = LogManager.getLogger(ECSResourceData.class);
	
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
		int result = strategy == null ? current : strategy.getResource(entity, resource, current);
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
			entity.getGame().executeEvent(new ResourceValueChange(entity, resource, get(), current, value),
					() -> {
						this.current = value;
						logger.debug("Modified " + resource + " for " + entity + " to " + value);
					});
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

	public ECSResource getResource() {
		return this.resource;
	}

	ECSResourceData copy(Entity copyTo) {
		ECSResourceData copy = new ECSResourceData(copyTo, resource);
		copy.current = this.current;
		copy.previousGet = this.previousGet;
		copy.strategy = this.strategy;
		return copy;
	}
	
}
