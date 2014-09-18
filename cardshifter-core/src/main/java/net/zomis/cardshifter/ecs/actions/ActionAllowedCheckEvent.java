package net.zomis.cardshifter.ecs.actions;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

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
	
}
