package com.cardshifter.gdx.api.incoming;

import com.cardshifter.gdx.api.messages.Message;

public class StartGameRequest extends Message {

	private final int opponent;
	private final String gameType;
	
	StartGameRequest() {
		this(-1, "");
	}
	
	public StartGameRequest(int opponent, String gameType) {
		super("startgame");
		this.opponent = opponent;
		this.gameType = gameType;
	}

	public int getOpponent() {
		return opponent;
	}
	
	public String getGameType() {
		return gameType;
	}
	
}
