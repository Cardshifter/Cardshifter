package net.zomis.cardshifter.ecs.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.base.Entity;

public class Resources {

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

}
