package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

import java.util.Collections;
import java.util.Map;

public class PlayerMessage extends Message {
//	SERVER: command: player, name: 'Bubu', properties: { hp: 23 }

	private final String name;
	private final Map<String, Integer> properties;
	private final int index;
	private final int id;

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
