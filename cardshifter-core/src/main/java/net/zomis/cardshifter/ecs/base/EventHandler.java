package net.zomis.cardshifter.ecs.base;

import java.util.function.Consumer;

public class EventHandler<T> {

	private final Consumer<T> consumer;
	private final boolean after;

	public EventHandler(Consumer<T> handler, boolean after) {
		this.consumer = handler;
		this.after = after;
	}
	
	public boolean isAfter() {
		return this.after;
	}
	
	@SuppressWarnings("unchecked")
	public void execute(Object event) {
		this.consumer.accept((T) event);
	}

}
