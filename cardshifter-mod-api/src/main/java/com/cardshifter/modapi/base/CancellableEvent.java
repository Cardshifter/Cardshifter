package com.cardshifter.modapi.base;

import com.cardshifter.modapi.events.IEvent;


public interface CancellableEvent extends IEvent {

	void setCancelled(boolean cancelled);
	boolean isCancelled();
	
}
