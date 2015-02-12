package com.cardshifter.modapi.attributes;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class AttributeValueChange implements IEvent {

	private final Entity entity;
	private final String previousValue;
	private final String previousActualValue;
	private final String newValue;
	private final ECSAttribute attribute;

	public AttributeValueChange(Entity entity, ECSAttribute attribute, String previousValue, String previousActualValue, String newValue) {
		this.entity = entity;
		this.attribute = attribute;
		this.previousValue = previousValue;
		this.previousActualValue = previousActualValue;
		this.newValue = newValue;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public String getNewValue() {
		return newValue;
	}
	
	public String getPreviousActualValue() {
		return previousActualValue;
	}
	
	public String getPreviousValue() {
		return previousValue;
	}
	
	public ECSAttribute getAttribute() {
		return attribute;
	}
	
}
