package com.cardshifter.modapi.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.cardshifter.modapi.base.Entity;

public final class Attributes {
	public static final ECSAttribute NAME = new ECSAttributeDefault("name");
	public static final ECSAttribute ID = new ECSAttributeDefault("id");
	public static final ECSAttribute FLAVOR = new ECSAttributeDefault("flavor");
	public static final ECSAttribute IMAGE_PATH = new ECSAttributeDefault("imagePath");
	
	private Attributes() {
		throw new UnsupportedOperationException();
	}

	public static void processAttributes(Entity card, Consumer<ECSAttributeData> consumer) {
        ECSAttributeMap attributes = card.getComponent(ECSAttributeMap.class);
        if (attributes != null) {
            attributes.getAttributes().forEach(consumer);
        }
	}

	public static Map<String, String> map(Entity playerFor) {
		HashMap<String, String> result = new HashMap<>();
		processAttributes(playerFor, data -> result.put(data.getAttribute().toString(), data.get()));
		return result;
	}

	public static AttributeRetriever retriever(ECSAttribute attribute) {
		return new AttributeRetriever(attribute);
	}

	public static String getOrDefault(Entity entity, ECSAttribute attribute, String defaultValue) {
		return AttributeRetriever.forAttribute(attribute).getOrDefault(entity, defaultValue);
	}

}
