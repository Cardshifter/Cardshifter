package net.zomis.cardshifter.ecs.actions;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class TargetableCheckEvent implements IEvent {

	private final ECSAction action;
	private final TargetSet targetSet;
	private final Entity target;
	private boolean allowed = true;

	public TargetableCheckEvent(ECSAction action, TargetSet targetSet, Entity target) {
		this.action = action;
		this.targetSet = targetSet;
		this.target = target;
	}
	
	public ECSAction getAction() {
		return action;
	}
	
	public Entity getTarget() {
		return target;
	}
	
	public TargetSet getTargetSet() {
		return targetSet;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}
	
}
