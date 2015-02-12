package com.cardshifter.modapi.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.cardshifter.modapi.base.Entity;

public final class Resources {
	private Resources() {
		throw new UnsupportedOperationException();
	}

	public static void processResources(Entity card, Consumer<ECSResourceData> consumer) {
		card.getComponent(ECSResourceMap.class).getResources().forEach(consumer);
	}

	public static Map<String, Integer> map(Entity playerFor) {
		HashMap<String, Integer> result = new HashMap<>();
		processResources(playerFor, data -> result.put(data.getResource().toString(), data.get()));
		return result;
	}

	public static ResourceRetriever retriever(ECSResource resource) {
		return new ResourceRetriever(resource);
	}

	public static int getOrDefault(Entity entity, ECSResource resource, int defaultValue) {
		return ResourceRetriever.forResource(resource).getOrDefault(entity, defaultValue);
	}

}
