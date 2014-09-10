package com.cardshifter.server.incoming;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestTargetsMessage extends Message {

	private final int gameId;
	private final int id;
	private final String action;

	public RequestTargetsMessage(@JsonProperty("gameId") int gameId, @JsonProperty("id") int id, @JsonProperty("action") String action) {
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
