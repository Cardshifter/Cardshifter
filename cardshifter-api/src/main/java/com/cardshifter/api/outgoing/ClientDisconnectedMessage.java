package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Inform that a player client has disconnected. */
public class ClientDisconnectedMessage extends Message {

	private String name;
	private int playerIndex;
	/** Constructor. (no params) */
	public ClientDisconnectedMessage() {
		this("", 0);
	}
	/**
	 * Constructor.
	 * @param name  Name of this player
	 * @param playerIndex  Index of this player
	 */
	public ClientDisconnectedMessage(String name, int playerIndex) {
		super("disconnect");
		this.name = name;
		this.playerIndex = playerIndex;
	}
	/** @return  Name of this player */
	public String getName() {
		return name;
	}
	/** @return  Index of this player  */
	public int getPlayerIndex() {
		return playerIndex;
	}

}
