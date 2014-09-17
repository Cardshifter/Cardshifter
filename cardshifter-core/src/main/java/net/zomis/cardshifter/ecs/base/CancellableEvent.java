package net.zomis.cardshifter.ecs.base;


public interface CancellableEvent extends IEvent {

	void setCancelled(boolean cancelled);
	boolean isCancelled();
	
}
