package com.cardshifter.server.outgoing;

import java.util.Collections;
import java.util.Map;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CardInfoMessage extends Message {
//	SERVER: command: card, zone: 3, id: 3, properties: { name: 'Biofsd', power: 3, health: 4, cardType: 'Creature', creatureType: 'B0T' }
	
	private final int zone;
	private int id;

	private final Map<String, String> properties;
	
	public CardInfoMessage(@JsonProperty("zoneid") int zoneId, @JsonProperty("id") int cardId, @JsonProperty("properties") Map<String, String> properties) {
		super("card");
		this.zone = zoneId;
		this.id = cardId;
		this.properties = properties;
	}
	
	public int getId() {
		return id;
	}
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	public int getZone() {
		return zone;
	}
	
	@Override
	public String toString() {
		return String.format("CardInfo: %d in zone %d - %s", this.id, this.zone, this.properties);
	}
	
}
