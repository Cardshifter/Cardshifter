package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityRemoveMessage extends Message {

	private final int entity;

	EntityRemoveMessage() {
		this(0);
	}

	public EntityRemoveMessage(@JsonProperty("entity") int entity) {
		super("entityRemoved");
		this.entity = entity;
	}
	
	public int getEntity() {
		return entity;
	}

	@Override
	public String toString() {
		return "EntityRemoveMessage [entity=" + entity + "]";
	}
	
}
