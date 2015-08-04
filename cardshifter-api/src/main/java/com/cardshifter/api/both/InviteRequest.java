package com.cardshifter.api.both;

import com.cardshifter.api.messages.Message;

/**
 * Request to invite a player to start a new game.
 */
public class InviteRequest extends Message {

	private int id;
	private String name;
	private String gameType;

	/** Constructor. (no params) */
	public InviteRequest() {
		this(0, "", "");
	}
	/**
	 * Constructor.
	 * @param id  The Id of this invite request
	 * @param name  The name of the player being invited
	 * @param gameType  The game type of this invite request
	 */
	public InviteRequest(int id, String name, String gameType) {
		super("inviteRequest");
		this.id = id;
		this.name = name;
		this.gameType = gameType;
	}
	/** @return  The Id of this invite request */
	public int getId() {
		return id;
	}
	/** @return  The name of the player being invited */
	public String getName() {
		return name;
	}
	/** @return  The game type of this invite request */
	public String getGameType() {
		return gameType;
	}

}
