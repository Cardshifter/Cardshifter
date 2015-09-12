package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

/**
 * Request to start a new game.
 * <p>
 * This is sent from the Client to the Server when this player invites another player (including AI) to start a new game of a chosen type.
 */
public class StartGameRequest extends Message {

	private final int opponent;
	private final String gameType;
	
	/** Constructor. (no params) */
	public StartGameRequest() {
		this(-1, "");
	}
	/**
	 * Constructor.
	 * @param opponent  The Id of the player entity being invited by this player
	 * @param gameType  The type / mod of the game chosen by this player
	 */
	public StartGameRequest(int opponent, String gameType) {
		super("startgame");
		this.opponent = opponent;
		this.gameType = gameType;
	}
	/** @return  The Id of the player entity being invited by this player */
	public int getOpponent() {
		return opponent;
	}
	/** @return  The type / mod of the game chosen by this player */
	public String getGameType() {
		return gameType;
	}
	
}
