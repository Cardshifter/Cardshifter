package com.cardshifter.modapi.attributes;

import java.util.Objects;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.modapi.base.Entity;



public class ECSAttributeData {

	private static final Logger logger = LogManager.getLogger(ECSAttributeData.class);
	
	private String previousGet;
	private String current;
	private ECSAttributeStrategy strategy;
	
	private final Entity entity;
	private final ECSAttribute attribute;
	
	public ECSAttributeData(Entity entity, ECSAttribute attribute) {
		this.entity = Objects.requireNonNull(entity, "Entity cannot be null. Make sure that AttributeMap is added to an entity.");
		this.attribute = Objects.requireNonNull(attribute, "Attribute cannot be null.");
	}
	
	public String get() {
		String result = strategy == null ? current : strategy.getAttribute(entity, current);
		if (!previousGet.equals(result)) {
			// Execute an event for UIs to update their values, or for other entities to react
			entity.getGame().getEvents().executePostEvent(new AttributeViewUpdate(entity, attribute, result));
		}
		previousGet = result;
		return result;
	}
	
	public void set(String value) {
		// Execute change event (for taking changing creature type, description, etc...).
		if (!this.current.equals(value)) {
			entity.getGame().executeEvent(new AttributeValueChange(entity, attribute, get(), current, value),
					() -> {
						this.current = value;
						logger.debug("Modified " + attribute + " for " + entity + " to " + value);
					});
		}
	}

	public void setStrategy(ECSAttributeStrategy strategy) {
		this.strategy = strategy;
	}
	
	public boolean contains(final CharSequence value) {
		Objects.requireNonNull(value, "value");
		return current.contains(value);
	}
	
	@Override
	public String toString() {
		return current;
	}

	public ECSAttribute getAttribute() {
		return this.attribute;
	}

	ECSAttributeData copy(Entity copyTo) {
		ECSAttributeData copy = new ECSAttributeData(copyTo, attribute);
		copy.current = this.current;
		copy.previousGet = this.previousGet;
		copy.strategy = this.strategy;
		return copy;
	}
	
}
