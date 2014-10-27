package com.cardshifter.modapi.base;

import com.cardshifter.modapi.events.IEvent;

public class PlayerEliminatedEvent implements IEvent, CancellableEvent {

	private final Entity entity;
	private final boolean declaredWinner;
	private final int resultPosition;
	private boolean cancelled;

	public PlayerEliminatedEvent(Entity entity, boolean declaredWinner, int resultPosition) {
		this.entity = entity;
		this.declaredWinner = declaredWinner;
		this.resultPosition = resultPosition;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public int getResultPosition() {
		return resultPosition;
	}
	
	public boolean isDeclaredWinner() {
		return declaredWinner;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
