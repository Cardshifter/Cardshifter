package com.cardshifter.modapi.attributes;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class AttributeViewUpdate implements IEvent {

	private final Entity entity;
	private final ECSAttribute attribute;
	private final String newValue;

	public AttributeViewUpdate(Entity entity, ECSAttribute attribute, String newValue) {
		this.entity = entity;
		this.attribute = attribute;
		this.newValue = newValue;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public String getNewValue() {
		return newValue;
	}
	
	public ECSAttribute getAttribute() {
		return attribute;
	}
	
}
