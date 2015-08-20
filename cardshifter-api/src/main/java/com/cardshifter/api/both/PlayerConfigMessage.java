package com.cardshifter.api.both;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.config.PlayerConfig;
import com.cardshifter.api.messages.Message;

/**
 * Player configuration for a given game.
 */
public class PlayerConfigMessage extends Message {

	private Map<String, PlayerConfig> configs;
	
	private int gameId;
	private String modName;
	
	/** Constructor. (no params) */
	public PlayerConfigMessage() {
		this(0, "", null);
	}
	/**
	 * Constructor.
	 * @param gameId  This game
	 * @param modName  The mod name for this game
	 * @param configs  Map of player name and applicable player configuration
	 */
	public PlayerConfigMessage(int gameId, String modName, Map<String, PlayerConfig> configs) {
		super("playerconfig");
		this.gameId = gameId;
		this.modName = modName;
		this.configs = configs;
	}
	/** @return  Map of player name and applicable player configuration */
	public Map<String, PlayerConfig> getConfigs() {
		return new HashMap<String, PlayerConfig>(configs);
	}
	/** @return  This game */
	public int getGameId() {
		return gameId;
	}
	/** @return  The mod name for this game */
	public String getModName() {
		return modName;
	}
	/** @return  This message as converted to String */
	@Override
	public String toString() {
	return "PlayerConfigMessage{" +
	        "configs=" + configs +
	        ", gameId=" + gameId +
	        ", modName='" + modName + '\'' +
	        '}';
	}
}
