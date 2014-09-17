package com.cardshifter.server.outgoing;

import java.util.Collections;
import java.util.Map;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerMessage extends Message {
//	SERVER: command: player, name: 'Bubu', properties: { hp: 23 }

	private final String name;
	private final Map<String, Integer> properties;
	private final int index;
	private final int id;

	@JsonCreator
	public PlayerMessage(@JsonProperty("id") int id, @JsonProperty("index") int index,
			@JsonProperty("name") String name, 
			@JsonProperty("properties") Map<String, Integer> properties) {
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
