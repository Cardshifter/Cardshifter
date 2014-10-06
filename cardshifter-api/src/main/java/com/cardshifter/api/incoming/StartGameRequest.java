package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class StartGameRequest extends Message {

	private final int opponent;
	private final String gameType;
	
	@JsonCreator
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
