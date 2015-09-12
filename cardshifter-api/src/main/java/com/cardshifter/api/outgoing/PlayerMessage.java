package com.cardshifter.api.outgoing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.messages.Message;

/** Message which contains properties about a player. */
public class PlayerMessage extends Message {

	private String name;
	private Map<String, Integer> properties;
	private int index;
	private int id;

	/** Constructor. (no params) */
	public PlayerMessage() {
		this(0, 0, "", new HashMap<String, Integer>());
	}
	/**
	 * Constructor.
	 * @param id  The Id of this player
	 * @param index  The index of this player
	 * @param name  The name of this player
	 * @param properties  The properties applicable to this player as Name/Value map
	 */
	public PlayerMessage(int id, int index, String name, Map<String, Integer> properties) {
		super("player");
		this.index = index;
		this.id = id;
		this.name = name;
		this.properties = properties;
	}
	/** @return  The name of this player */
	public String getName() {
		return name;
	}
	/** @return  The properties applicable to this player as Name/Value map */
	public Map<String, Integer> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	/** @return  The index of this player */
	public int getIndex() {
		return index;
	}
	/**@return  The Id of this player  */
	public int getId() {
		return id;
	}
	/** 
	 * @return  This message as converted to String 
	 * @example  <code>SERVER: command: player, name: 'Bubu', properties: { hp: 23 }</code>
	 */
	@Override
	public String toString() {
		return "Player Info: " + name + " - " + properties;
	}
}
