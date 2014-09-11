package com.cardshifter.server.outgoing;

import java.util.Collections;
import java.util.Map;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerMessage extends Message {
//	SERVER: command: player, name: 'Bubu', properties: { hp: 23 }

	private final String name;
	private final Map<String, String> properties;

	@JsonCreator
	public PlayerMessage(@JsonProperty("name") String name, @JsonProperty("properties") Map<String, String> properties) {
		super("player");
		this.name = name;
		this.properties = properties;
	}

	public String getName() {
		return name;
	}
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	@Override
	public String toString() {
		return String.format("Player Info: %s - %s", this.name, this.properties);
	}
	
}
