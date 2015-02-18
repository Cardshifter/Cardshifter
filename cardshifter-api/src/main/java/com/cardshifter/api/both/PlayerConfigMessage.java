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
	
	private final int gameId;
	private final String modName;

	PlayerConfigMessage() {
		this(0, "", null);
	}

	public PlayerConfigMessage(int gameId, String modName, Map<String, Object> configs) {
		super("playerconfig");
		this.gameId = gameId;
		this.modName = modName;
		this.configs = configs;
	}
	
	public Map<String, Object> getConfigs() {
		return new HashMap<String, Object>(configs);
	}
	
	public int getGameId() {
		return gameId;
	}

	public String getModName() {
		return modName;
	}
}
