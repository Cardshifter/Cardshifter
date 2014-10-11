package net.zomis.cardshifter.ecs;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.Resources;


public class EntitySerialization {

	private static final ComponentRetriever<CreatureTypeComponent> creatureType = ComponentRetriever.retreiverFor(CreatureTypeComponent.class);
	
	public static CardInfoMessage serialize(int zoneId, Entity entity) {
		return new CardInfoMessage(zoneId, entity.getId(), serialize(entity));
	}
	
	public static Map<String, Object> serialize(Entity entity) {
		Map<String, Object> result = new HashMap<>();
		result.putAll(Resources.map(entity));
		if (creatureType.has(entity)) {
			result.put("creatureType", creatureType.get(entity).getCreatureType());
		}
		return result;
	}
	
}
