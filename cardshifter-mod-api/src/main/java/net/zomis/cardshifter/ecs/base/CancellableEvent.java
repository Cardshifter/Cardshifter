package net.zomis.cardshifter.ecs.base;

import net.zomis.cardshifter.ecs.events.IEvent;


public interface CancellableEvent extends IEvent {

	void setCancelled(boolean cancelled);
	boolean isCancelled();
	
}
