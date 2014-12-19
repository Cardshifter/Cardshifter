package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

public class NewGameMessage extends Message {

	private final int gameId;
	private final int playerIndex;

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
