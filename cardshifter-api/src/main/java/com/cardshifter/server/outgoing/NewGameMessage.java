package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class NewGameMessage extends Message {

	private final int gameId;
	private final int playerIndex;

	@JsonCreator
	NewGameMessage() {
		this(0, 0);
	}
	public NewGameMessage(int gameId, int playerIndex) {
		super("newgame");
		this.gameId = gameId;
		this.playerIndex = playerIndex;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public int getPlayerIndex() {
		return playerIndex;
	}

}
