package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoneChangeMessage extends Message {

	private final int entity;
	private final int sourceZone;
	private final int destinationZone;

	@JsonCreator
	public ZoneChangeMessage(@JsonProperty("entity") int entity, 
			@JsonProperty("sourceZone") int sourceZone, @JsonProperty("destinationZone") int destinationZone) {
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
