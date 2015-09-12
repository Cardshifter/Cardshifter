package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

import java.util.Collections;
import java.util.Map;

/**
 * Message containing information about a specific card to be used by the client.
 */
public class CardInfoMessage extends Message {
//	SERVER: command: card, zone: 3, id: 3, properties: { name: 'Biofsd', power: 3, health: 4, cardType: 'Creature', creatureType: 'B0T' }
	
	private int zone;
	private int id;
	private Map<String, Object> properties;

	/** Constructor. (no params) */
	public CardInfoMessage() {
		this(0, 0, null);
	}
	/**
	 * Constructor.
	 * @param zoneId  The zone where this card is
	 * @param cardId  The Id of this card
	 * @param properties  Object containing the properties of this card, can be null
	 */
	public CardInfoMessage(int zoneId, int cardId, Map<String, Object> properties) {
		super("card");
		this.zone = zoneId;
		this.id = cardId;
		this.properties = properties;
	}
	/** @return  The Id of this card */
	public int getId() {
		return id;
	}
	/** @return  Object containing the properties of this card, returns null if null */
	public Map<String, Object> getProperties() {
		return properties == null ? null : Collections.unmodifiableMap(properties);
	}
	/** @return  The zone where this card is */
	public int getZone() {
		return zone;
	}
	/**
	 * @return  The name of this action requested to be performed
	 * @example  <code>SERVER: command: card, zone: 3, id: 3, properties: { name: 'Conscript', power: 3, health: 4, cardType: 'Creature', creatureType: 'Bio' }</code>
	 */
	@Override
	public String toString() {
		return "CardInfo: " + id + " in zone " + zone + " - " + this.properties;
	}
	
}
