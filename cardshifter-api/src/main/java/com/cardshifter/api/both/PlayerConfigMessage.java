package com.cardshifter.api.both;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public class PlayerConfigMessage extends Message {

	@JsonTypeInfo(include = As.PROPERTY, use = Id.NAME, property = "_type")
	private final Map<String, Object> configs;
	
	PlayerConfigMessage() {
		this(null);
	}

	public PlayerConfigMessage(Map<String, Object> configs) {
		super("playerconfig");
		this.configs = configs;
	}
	
	public Map<String, Object> getConfigs() {
		return new HashMap<>(configs);
	}
	
}
