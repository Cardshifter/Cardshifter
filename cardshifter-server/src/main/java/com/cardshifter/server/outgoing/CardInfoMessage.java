package com.cardshifter.server.outgoing;

import java.util.Collections;
import java.util.Map;

import com.cardshifter.core.Card;
import com.cardshifter.core.LuaTools;
import com.cardshifter.server.incoming.Message;

public class CardInfoMessage extends Message {
//	SERVER: command: card, zone: 3, id: 3, properties: { name: 'Biofsd', power: 3, health: 4, cardType: 'Creature', creatureType: 'B0T' }
	
	private final int zone;
	private int id;

	private final Map<String, String> properties;
	
	public CardInfoMessage(Card card) {
		this.zone = card.getZone().getId();
		this.id = card.getId();
		this.properties = LuaTools.tableToJava(card.data);
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
	
}
