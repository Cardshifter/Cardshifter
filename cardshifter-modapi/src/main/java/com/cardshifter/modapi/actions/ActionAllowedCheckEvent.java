package com.cardshifter.modapi.actions;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class ActionAllowedCheckEvent implements IEvent {

	private final Entity entity;
	private final ECSAction action;
	private final Entity performer;
	private boolean allowed = true;
	
	public ActionAllowedCheckEvent(Entity owner, ECSAction action, Entity performer) {
		this.entity = owner;
		this.action = action;
		this.performer = performer;
	}
	
	public ECSAction getAction() {
		return action;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public Entity getPerformer() {
		return performer;
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	@Override
	public String toString() {
		return "ActionAllowedCheckEvent [entity=" + entity + ", action="
				+ action + ", performer=" + performer + ", allowed=" + allowed
				+ "]";
	}
	
}
