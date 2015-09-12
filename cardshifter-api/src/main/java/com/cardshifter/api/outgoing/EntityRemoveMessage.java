package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Message from server that an entity has been removed. */
public class EntityRemoveMessage extends Message {

	private int entity;
	/** Constructor. (no params) */
	public EntityRemoveMessage() {
		this(0);
	}
	/**
	 * Constructor.
	 * @param entity  The Id of the removed entity
	 */
	public EntityRemoveMessage(int entity) {
		super("entityRemoved");
		this.entity = entity;
	}
	/** @return  The Id of the removed entity */
	public int getEntity() {
		return entity;
	}
	/** @return  This message as converted to String */
	@Override
	public String toString() {
		return "EntityRemoveMessage [entity=" + entity + "]";
	}
	
}
