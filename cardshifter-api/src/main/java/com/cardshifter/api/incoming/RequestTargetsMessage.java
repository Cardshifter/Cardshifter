package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestTargetsMessage extends Message {

	private final int gameId;
	private final int id;
	private final String action;

	RequestTargetsMessage() {
		this(0, 0, "");
	}

	public RequestTargetsMessage(int gameId, int id, String action) {
		super("requestTargets");
		this.gameId = gameId;
		this.id = id;
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public int getId() {
		return id;
	}


}
