package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Communicates that an entity has changed zones. */
public class ZoneChangeMessage extends Message {

	private int entity;
	private int sourceZone;
	private int destinationZone;

	/** Constructor. (no params) */
    	public ZoneChangeMessage() {
		this(0, 0, 0);
	}
	/**
	 * Constructor. 
	 * @param entity  This entity
	 * @param sourceZone  The zone where this entity is before the change
	 * @param destinationZone  The zone where this entity is after the change
	 */
	public ZoneChangeMessage(int entity, int sourceZone, int destinationZone) {
		super("zoneChange");
		this.entity = entity;
		this.sourceZone = sourceZone;
		this.destinationZone = destinationZone;
	}
	/** @return  This entity */
	public int getEntity() {
		return entity;
	}
	/** @return  The zone where the entity is before the change */
	public int getSourceZone() {
		return sourceZone;
	}
	/** @return  The zone where the entity is after the change */
	public int getDestinationZone() {
		return destinationZone;
	}
	/** @return  This message as converted to String  */
	@Override
	public String toString() {
		return "ZoneChangeMessage ["
			+ "entity=" + entity 
			+ ", sourceZone=" + sourceZone 
			+ ", destinationZone=" + destinationZone 
		+ "]";
	}
	
}
