package com.cardshifter.modapi.base;

import java.util.Objects;
import java.util.Random;

import com.cardshifter.modapi.events.IEvent;


public abstract class Component {

	private Entity entity;

	void setEntity(Entity entity) {
		if (this.entity != null) {
			throw new IllegalStateException("Component " + this + " is already connected to another entity.");
		}
		this.entity = entity;
		// TODO: Is it possible to avoid having a Component know about its entity?
	}
	
	protected final <T extends IEvent> T executeEvent(T event) {
		return getGame().getEvents().executePostEvent(event);
	}
	
	private ECSGame getGame() {
		Objects.requireNonNull(entity, "Component is not connected to an entity.");
		return entity.getGame();
	}

	protected final <T extends IEvent> T executeEvent(T event, Runnable runInBetween) {
		return entity.getGame().executeEvent(event, runInBetween);
	}
	
	protected final <T extends CancellableEvent> T executeCancellableEvent(T event, Runnable runInBetween) {
		return entity.getGame().executeCancellableEvent(event, runInBetween);
	}
	
	protected final Entity getEntity() {
		return entity;
	}
	
	protected final Random getRandom() {
		return getEntity().getGame().getRandom();
	}
	
}
