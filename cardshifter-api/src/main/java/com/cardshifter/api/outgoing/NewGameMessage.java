package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class NewGameMessage extends Message {

	private int gameId;
	private int playerIndex;

	public NewGameMessage() {
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
