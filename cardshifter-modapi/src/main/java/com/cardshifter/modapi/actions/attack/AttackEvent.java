package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.base.CancellableEvent;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class AttackEvent implements IEvent, CancellableEvent {

	private final Entity attacker;
	private final Entity target;
	private boolean cancelled;

	public AttackEvent(Entity attacker, Entity target) {
		this.attacker = attacker;
		this.target = target;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public Entity getAttacker() {
		return attacker;
	}
	
	public Entity getTarget() {
		return target;
	}
	
}
