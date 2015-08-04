package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
/** Communicates a usable action. */
public class UsableActionMessage extends Message {

	private int id;
	private String action;
	private boolean targetRequired;
	private int targetId;
	
	/** Constructor. (no params) */
    	public UsableActionMessage() {
		this(0, "", false, 0);
	}
	/**
	 * Constructor.
	 * <p>
	 * Used for actions where a target is required.
	 * 
	 * @param id  This entity
	 * @param action  This action
	 * @param targetRequired  Whether or not a target is required for this action
	 * @param target  The target of this action
	 */
	public UsableActionMessage(int id, String action, boolean targetRequired, int target) {
		super("useable");
		this.id = id;
		this.action = action;
		this.targetRequired = targetRequired;
		this.targetId = target;
	}
	/** 
	 * Constructor.
	 * <p>
	 * Used for actions where a target is not required.
	 * 
	 * @param entityId  This entity
	 * @param name  This action
	 * @param needsTarget  Whether of not this action needs a target
	 */
	public UsableActionMessage(int entityId, String name, boolean needsTarget) {
		this(entityId, name, needsTarget, 0);
	}
	/** @return  This action */
	public String getAction() {
		return action;
	}
	/** @return  This entity */
	public int getId() {
		return id;
	}
	/** @return  Whether or not a target is required */
	public boolean isTargetRequired() {
		return targetRequired;
	}
	/** @return  The target of this action */
	public int getTargetId() {
		return targetId;
	}
	/** @return  This message as converted to String  */
	@Override
	public String toString() {
		return "UsableActionMessage ["
				+ id=" + id 
				+ ", action=" + action
				+ ", targetRequired=" + targetRequired 
				+ ", targetId=" + targetId 
				+ "]";
	}

}
