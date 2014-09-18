package net.zomis.cardshifter.ecs.actions;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class ActionPerformEvent implements IEvent {

	private final Entity entity;
	private final ECSAction action;
	private final Entity performer;

	public ActionPerformEvent(Entity owner, ECSAction ecsAction, Entity performer) {
		this.entity = owner;
		this.action = ecsAction;
		this.performer = performer;
	}
	
	public ECSAction getAction() {
		return action;
	}
	
	/**
	 * @return The entity that this Action is attached to. Is the same as <code>getAction().getEntity()</code>
	 */
	public Entity getEntity() {
		return entity;
	}
	
	/**
	 * Get the entity that activated this Action (normally a Player)
	 * @return
	 */
	public Entity getPerformer() {
		return performer;
	}
}
