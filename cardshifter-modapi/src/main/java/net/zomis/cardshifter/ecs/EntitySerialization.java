package net.zomis.cardshifter.ecs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.zomis.cardshifter.ecs.effects.EffectComponent;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.Resources;


public class EntitySerialization {

	private static final ComponentRetriever<CreatureTypeComponent> creatureType = ComponentRetriever.retreiverFor(CreatureTypeComponent.class);
	private static final ComponentRetriever<EffectComponent> effect = ComponentRetriever.retreiverFor(EffectComponent.class);
	
	public static CardInfoMessage serialize(int zoneId, Entity entity) {
		return new CardInfoMessage(zoneId, entity.getId(), serialize(entity));
	}
	
	public static Map<String, Object> serialize(Entity entity) {
		Map<String, Object> result = new HashMap<>();
		result.putAll(Resources.map(entity));
		result.putAll(Attributes.map(entity));
		saveIfHave(entity, result, creatureType, "creatureType", comp -> String.join(" ", comp.getCreatureTypes()));
		saveIfHave(entity, result, effect, "effect", comp -> comp.getDescription());
		return result;
	}

	private static <T extends Component> void saveIfHave(Entity entity, Map<String, Object> result, ComponentRetriever<T> retriever,
			String key, Function<T, Object> save) {
		if (retriever.has(entity)) {
			result.put(key, save.apply(retriever.get(entity)));
		}
	}
	
}
