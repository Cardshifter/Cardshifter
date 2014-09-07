package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UseableActionMessage extends Message {

	private final int id;
	private final String action;
	private final boolean targetRequired;

	@JsonCreator
	public UseableActionMessage(@JsonProperty("id") int id, @JsonProperty("action") String action, @JsonProperty("targetRequired") boolean targetRequired) {
		super("useable");
		this.id = id;
		this.action = action;
		this.targetRequired = targetRequired;
	}
	
	public String getAction() {
		return action;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isTargetRequired() {
		return targetRequired;
	}

	@Override
	public String toString() {
		return "UseableActionMessage [id=" + id + ", action=" + action + ", targetRequired=" + targetRequired + "]";
	}

}
