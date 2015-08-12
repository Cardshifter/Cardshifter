package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

/**
 * Request available targets for a specific action to be performed by an entity.
 * <p>
 * These in-game messages request a list of al available targets for a given action and entity.
 * The client uses this request in order to point out targets (hopefully with a visual aid such as highlighting targets)
 * that an entity (such as a creature card, or a player) can perform an action on (for example attack or enchant a card.
 */
public class RequestTargetsMessage extends Message {
	
	private final int gameId;
	private final int id;
	private final String action;

	/** Constructor. (no params) */
	public RequestTargetsMessage() {
		this(0, 0, "");
	}
	
	/**
	 * Constructor.
	 * @param gameId  The Id of this game currently being played
	 * @param id  The Id of this entity which requests to perform an action
	 * @param action  The name of this action requested to be performed
	 */
	public RequestTargetsMessage(int gameId, int id, String action) {
		super("requestTargets");
		this.gameId = gameId;
		this.id = id;
		this.action = action;
	}
	
	/** @return  The Id of this game currently being played */
	public int getGameId() {
		return gameId;
	}
	/** @return  The Id of this entity which requests to perform an action */
	public int getId() {
		return id;
	}
	/** @return  The name of this action requested to be performed */
	public String getAction() {
		return action;
	}
}
