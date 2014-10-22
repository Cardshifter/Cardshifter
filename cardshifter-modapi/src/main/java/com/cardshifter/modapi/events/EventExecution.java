package com.cardshifter.modapi.events;

import com.cardshifter.modapi.base.CancellableEvent;

public interface EventExecution {
	
	<T extends IEvent> T executePostEvent(T event);
	<T extends IEvent> T executePreEvent(T event);
	<T extends IEvent> T executeEvent(T event, Runnable runInBetween);
	<T extends CancellableEvent> T executeCancellableEvent(T event, Runnable runInBetween);
	
}
