package com.cardshifter.gdx.api.both;

import com.cardshifter.gdx.api.messages.Message;

import java.util.HashMap;
import java.util.Map;

public class PlayerConfigMessage extends Message {

	private final Map<String, Object> configs;
	
	private final int gameId;
	
	PlayerConfigMessage() {
		this(0, null);
	}

	public PlayerConfigMessage(int gameId, Map<String, Object> configs) {
		super("playerconfig");
		this.gameId = gameId;
		this.configs = configs;
	}
	
	public Map<String, Object> getConfigs() {
		return new HashMap<String, Object>(configs);
	}
	
	public int getGameId() {
		return gameId;
	}
	
}
