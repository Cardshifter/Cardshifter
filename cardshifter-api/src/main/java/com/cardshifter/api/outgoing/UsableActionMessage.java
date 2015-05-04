package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class UsableActionMessage extends Message {

	private int id;
	private String action;
	private boolean targetRequired;
	private int targetId;

    public UsableActionMessage() {
		this(0, "", false, 0);
	}

	public UsableActionMessage(int id, String action, boolean targetRequired, int target) {
		super("useable");
		this.id = id;
		this.action = action;
		this.targetRequired = targetRequired;
		this.targetId = target;
	}
	
	public UsableActionMessage(int entityId, String name, boolean needsTarget) {
		this(entityId, name, needsTarget, 0);
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
		return targetId;
	}

	@Override
	public String toString() {
		return "UsableActionMessage [id=" + id + ", action=" + action
				+ ", targetRequired=" + targetRequired + ", targetId="
				+ targetId + "]";
	}

}
