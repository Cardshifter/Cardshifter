package com.cardshifter.gdx.api.outgoing;

import java.util.Collections;
import java.util.Map;

import com.cardshifter.gdx.api.messages.Message;

public class CardInfoMessage extends Message {
//	SERVER: command: card, zone: 3, id: 3, properties: { name: 'Biofsd', power: 3, health: 4, cardType: 'Creature', creatureType: 'B0T' }
	
	private final int zone;
	private int id;

	private final Map<String, Object> properties;
	
	public CardInfoMessage(int zoneId, int cardId, Map<String, Object> properties) {
		super("card");
		this.zone = zoneId;
		this.id = cardId;
		this.properties = properties;
	}
	
	public int getId() {
		return id;
	}
	
	public Map<String, Object> getProperties() {
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
