package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
/** Message stating that a new game has begun. */
public class NewGameMessage extends Message {

	private int gameId;
	private int playerIndex;
	/** Constructor. (no params) */
	public NewGameMessage() {
		this(0, 0);
	}
	/**
	 * Constructor. 
	 * @param gameId  The Id of this game
	 * @param playerIndex  The index of this player
	 */
	public NewGameMessage(int gameId, int playerIndex) {
		super("newgame");
		this.gameId = gameId;
		this.playerIndex = playerIndex;
	}
	/** @return  The Id of this game */
	public int getGameId() {
		return gameId;
	}
	/** @return  The index of this player */
	public int getPlayerIndex() {
		return playerIndex;
	}

}
