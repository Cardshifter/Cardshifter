package net.zomis.cardshifter.ecs.events;

import net.zomis.cardshifter.ecs.base.CancellableEvent;

public interface EventExecution {
	
	<T extends IEvent> T executePostEvent(T event);
	<T extends IEvent> T executePreEvent(T event);
	<T extends IEvent> T executeEvent(T event, Runnable runInBetween);
	<T extends CancellableEvent> T executeCancellableEvent(T event, Runnable runInBetween);
	
}
