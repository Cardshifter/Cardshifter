package com.cardshifter.api.incoming;

import com.cardshifter.api.abstr.CardMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class UseAbilityMessage extends CardMessage {

	private final int id;
	private final String action;
	private final int gameId;
	private final int target;

	@JsonCreator
	public UseAbilityMessage(@JsonProperty("gameId") int gameId, @JsonProperty("id") int id, 
			@JsonProperty("action") String action, @JsonProperty("target") int target) {
		super("use");
		this.id = id;
		this.action = action;
		this.gameId = gameId;
		this.target = target;
	}
	
	public String getAction() {
		return action;
	}
	
	public int getId() {
		return id;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public int getTarget() {
		return target;
	}

}
