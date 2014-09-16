package net.zomis.cardshifter.ecs.actions;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class ActionPerformEvent implements IEvent {

	private final Entity entity;
	private final ECSAction action;

	public ActionPerformEvent(Entity owner, ECSAction ecsAction) {
		this.entity = owner;
		this.action = ecsAction;
	}
	
	public ECSAction getAction() {
		return action;
	}
	
	public Entity getEntity() {
		return entity;
	}
}
