package com.cardshifter.modapi.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.modapi.base.CancellableEvent;

public class EventExecutor implements EventExecution {

	private static final Logger logger = LogManager.getLogger(EventExecutor.class);
	
	protected final Map<Class<? extends IEvent>, Collection<EventHandler<?>>> bindings;
	
	public EventExecutor() {
		this.bindings = new HashMap<Class<? extends IEvent>, Collection<EventHandler<?>>>();
	}
	
	private <T extends IEvent> T executeEventInternal(T event, Predicate<EventHandler<?>> predicate) {
		Collection<EventHandler<?>> handlers = this.bindings.get(event.getClass());
		if (handlers != null) {
			List<EventHandler<?>> interestedHandlers = handlers.stream().filter(predicate).collect(Collectors.toList());
			ListIterator<EventHandler<?>> iterator = interestedHandlers.listIterator();
			while (iterator.hasNext()) {
				EventHandler<?> performer = iterator.next();
//				int index = iterator.nextIndex();
//				logger.info("Handling " + event + " " + index + " / " + interestedHandlers.size() + ": " + performer);
				performer.execute(event);
			}
		}
		return event;
	}
	
	@Override
	public <T extends IEvent> T executePostEvent(T event) {
		logger.debug("Execute pre event " + event);
		return executeEventInternal(event, eh -> eh.isAfter());
	}

	@Override
	public <T extends IEvent> T executePreEvent(T event) {
		logger.debug("Execute post event " + event);
		return executeEventInternal(event, eh -> !eh.isAfter());
	}

	/**
	 * Execute a pre-event, perform something, then execute a post-event.
	 * 
	 * @param event Event to execute
	 * @param runInBetween What to do between pre- and post- events.
	 * @return The event that was executed
	 */
	@Override
	public <T extends IEvent> T executeEvent(T event, Runnable runInBetween) {
		executePreEvent(event);
		runInBetween.run();
		executePostEvent(event);
		return event;
	}
	
	public <T extends IEvent> T executeEvent(T event, Consumer<T> runInBetween) {
		executePreEvent(event);
		runInBetween.accept(event);
		executePostEvent(event);
		return event;
	}
	
	@Override
	public <T extends CancellableEvent> T executeCancellableEvent(T event, Runnable runInBetween) {
		executePreEvent(event);
		if (!event.isCancelled()) {
			runInBetween.run();
			executePostEvent(event);
		}
		return event;
	}
	
	public <T extends IEvent> void registerHandler(Class<T> realParam, EventHandler<T> handler) {
		if (!this.bindings.containsKey(realParam)) {
			this.bindings.put(realParam, createCollection());
		}
		Collection<EventHandler<?>> eventHandlersForEvent = this.bindings.get(realParam);
		eventHandlersForEvent.add(handler);
	}

	protected Collection<EventHandler<?>> createCollection() {
		return new ArrayList<EventHandler<?>>();
	}

	public void clearListeners() {
		this.bindings.clear();
	}

	public void removeHandler(EventHandler<?> listener) {
		for (Entry<Class<? extends IEvent>, Collection<EventHandler<?>>> ee : bindings.entrySet()) {
			Iterator<EventHandler<?>> it = ee.getValue().iterator();
			while (it.hasNext()) {
				EventHandler<?> curr = it.next();
				if (curr == listener) {
					it.remove();
				}
			}
		}
	}
	
	public void removeListenersWithIdentifier(Object identifier) {
		for (Entry<Class<? extends IEvent>, Collection<EventHandler<?>>> ee : bindings.entrySet()) {
			ee.getValue().removeIf(eh -> eh.getIdentifier() == identifier);
		}
	}
	
	public <T extends IEvent> EventHandler<T> registerHandlerAfter(Object identifier, Class<T> realParam, Consumer<T> handler) {
		EventHandler<T> listener = new EventHandler<T>(identifier, handler, true);
		registerHandler(realParam, listener);
		return listener;
	}

	public <T extends IEvent> EventHandler<T> registerHandlerBefore(Object identifier, Class<T> realParam, Consumer<T> handler) {
		EventHandler<T> listener = new EventHandler<T>(identifier, handler, false);
		registerHandler(realParam, listener);
		return listener;
	}

}