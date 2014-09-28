package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UseableActionMessage extends Message {

	private final int id;
	private final String action;
	private final boolean targetRequired;
	private final Integer targetId;

	@JsonCreator
	public UseableActionMessage(@JsonProperty("id") int id, @JsonProperty("action") String action, @JsonProperty("targetRequired") boolean targetRequired,
			@JsonProperty("targetId") Integer target) {
		super("useable");
		this.id = id;
		this.action = action;
		this.targetRequired = targetRequired;
		this.targetId = target;
	}
	
	public UseableActionMessage(int entityId, String name, boolean needsTarget) {
		this(entityId, name, needsTarget, null);
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
	
	public int getTargetId() {
		return targetId == null ? 0 : targetId;
	}

	@Override
	public String toString() {
		return "UseableActionMessage [id=" + id + ", action=" + action
				+ ", targetRequired=" + targetRequired + ", targetId="
				+ targetId + "]";
	}

}
