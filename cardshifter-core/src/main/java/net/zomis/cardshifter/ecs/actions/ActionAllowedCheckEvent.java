package net.zomis.cardshifter.ecs.actions;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class ActionAllowedCheckEvent implements IEvent {

	private final Entity entity;
	private final ECSAction action;
	private boolean allowed = true;
	
	public ActionAllowedCheckEvent(Entity owner, ECSAction action) {
		this.entity = owner;
		this.action = action;
	}
	
	public ECSAction getAction() {
		return action;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}
	
}
