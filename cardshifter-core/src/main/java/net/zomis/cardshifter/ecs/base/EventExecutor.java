package net.zomis.cardshifter.ecs.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventExecutor {

	protected final Map<Class<? extends IEvent>, Collection<EventHandler<?>>> bindings;
	
	public EventExecutor() {
		this.bindings = new HashMap<Class<? extends IEvent>, Collection<EventHandler<?>>>();
	}
	
	private <T extends IEvent> T executeEvent(T event, Predicate<EventHandler<?>> predicate) {
		Collection<EventHandler<?>> handlers = this.bindings.get(event.getClass());
		if (handlers != null) {
			handlers.stream().filter(predicate).forEachOrdered(e -> e.execute(event));
		}
		return event;
	}
	
	public <T extends IEvent> T executePostEvent(T event) {
		return executeEvent(event, eh -> eh.isAfter());
	}

	public <T extends IEvent> T executePreEvent(T event) {
		return executeEvent(event, eh -> !eh.isAfter());
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
	
	public <T extends IEvent> EventHandler<T> registerHandlerAfter(Class<T> realParam, Consumer<T> handler) {
		EventHandler<T> listener = new EventHandler<T>(handler, true);
		registerHandler(realParam, listener);
		return listener;
	}

	public <T extends IEvent> EventHandler<T> registerHandlerBefore(Class<T> realParam, Consumer<T> handler) {
		EventHandler<T> listener = new EventHandler<T>(handler, false);
		registerHandler(realParam, listener);
		return listener;
	}

}