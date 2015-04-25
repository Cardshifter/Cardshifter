package com.cardshifter.api.outgoing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.messages.Message;

public class PlayerMessage extends Message {
//	SERVER: command: player, name: 'Bubu', properties: { hp: 23 }

	private String name;
	private Map<String, Integer> properties;
	private int index;
	private int id;

	public PlayerMessage() {
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
		return "Player Info: " + name + " - " + properties;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getId() {
		return id;
	}
}
