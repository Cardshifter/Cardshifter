package com.cardshifter.server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerHandler<T extends IdObject> {
	
	private final AtomicInteger ids = new AtomicInteger(0);
	private final Map<Integer, T> map = new ConcurrentHashMap<>();

	public void add(T object) {
		map.put(object.getId(), object);
	}

	public int newId() {
		return ids.incrementAndGet();
	}

	public T get(int id) {
		return map.get(id);
	}

}
