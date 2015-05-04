package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ZoneChangeMessage extends Message {

	private int entity;
	private int sourceZone;
	private int destinationZone;

    public ZoneChangeMessage() {
		this(0, 0, 0);
	}

	public ZoneChangeMessage(int entity, int sourceZone, int destinationZone) {
		super("zoneChange");
		this.entity = entity;
		this.sourceZone = sourceZone;
		this.destinationZone = destinationZone;
	}
	
	public int getEntity() {
		return entity;
	}
	
	public int getSourceZone() {
		return sourceZone;
	}

	public int getDestinationZone() {
		return destinationZone;
	}

	@Override
	public String toString() {
		return "ZoneChangeMessage [entity=" + entity + ", sourceZone="
				+ sourceZone + ", destinationZone=" + destinationZone + "]";
	}
	
}
