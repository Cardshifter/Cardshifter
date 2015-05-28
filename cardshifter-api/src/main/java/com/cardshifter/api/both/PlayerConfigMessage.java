package com.cardshifter.api.both;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.config.PlayerConfig;
import com.cardshifter.api.messages.Message;

public class PlayerConfigMessage extends Message {

	private Map<String, PlayerConfig> configs;
	
	private int gameId;
	private String modName;

	public PlayerConfigMessage() {
		this(0, "", null);
	}

	public PlayerConfigMessage(int gameId, String modName, Map<String, PlayerConfig> configs) {
		super("playerconfig");
		this.gameId = gameId;
		this.modName = modName;
		this.configs = configs;
	}
	
	public Map<String, PlayerConfig> getConfigs() {
		return new HashMap<String, PlayerConfig>(configs);
	}
	
	public int getGameId() {
		return gameId;
	}

	public String getModName() {
		return modName;
	}

    @Override
    public String toString() {
        return "PlayerConfigMessage{" +
                "configs=" + configs +
                ", gameId=" + gameId +
                ", modName='" + modName + '\'' +
                '}';
    }
}
