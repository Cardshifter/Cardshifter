package com.cardshifter.server.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.cardshifter.api.IdObject;

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

	public Map<Integer, T> all() {
		return Collections.unmodifiableMap(map);
	}
	
	public void remove(T object) {
		Objects.requireNonNull(object, "Object to remove cannot be null");
		map.remove(object.getId());
	}

}
