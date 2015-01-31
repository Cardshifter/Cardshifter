package com.cardshifter.api.outgoing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerMessage extends Message {
//	SERVER: command: player, name: 'Bubu', properties: { hp: 23 }

	private final String name;
	private final Map<String, Integer> properties;
	private final int index;
	private final int id;

	PlayerMessage() {
		this(0, 0, "", new HashMap<String, Integer>());
	}

	public PlayerMessage(int id, int index, String name, Map<String, Integer> properties) {
		super("player");
		this.index = index;
		this.id = id;
		this.name = name;
		this.properties = properties;
	}

	public String getName() {
		return name;
	}
	
	public Map<String, Integer> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	@Override
	public String toString() {
		return String.format("Player Info: %s - %s", this.name, this.properties);
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getId() {
		return id;
	}
}
