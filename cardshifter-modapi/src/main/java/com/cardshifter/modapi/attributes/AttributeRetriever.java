package com.cardshifter.modapi.attributes;

import java.util.Objects;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;

public class AttributeRetriever {

	private final ECSAttribute attribute;

	AttributeRetriever(ECSAttribute attribute) {
		this.attribute = attribute;
	}
	
	public static AttributeRetriever forAttribute(ECSAttribute attribute) {
		return new AttributeRetriever(attribute);
	}

	public String getFor(Entity entity) {
		return attrMap(entity).getAttribute(attribute).get();
	}

	private ECSAttributeMap attrMap(Entity entity) {
		Objects.requireNonNull(entity, "Cannot retrieve attribute map for null entity");
		if (entity.isRemoved()) {
			throw new IllegalArgumentException(entity + " has been marked for removal.");
		}
		ECSAttributeMap map = entity.getComponent(ECSAttributeMap.class);
		return Objects.requireNonNull(map, entity + " does not have a attribute component: " + entity.getSuperComponents(Component.class));
	}
	
	public boolean has(Entity entity) {
		Objects.requireNonNull(entity, "Cannot retrieve attribute map for null entity");
		ECSAttributeMap map = entity.getComponent(ECSAttributeMap.class);
		return map != null && attrFor(entity) != null;
	}

	public ECSAttributeData attrFor(Entity entity) {
		return attrMap(entity).getAttribute(attribute);
	}

	public ECSAttribute getAttribute() {
		return attribute;
	}

	public String getOrDefault(Entity entity, String defaultValue) {
		Objects.requireNonNull(entity, "Cannot retrieve attribute map for null entity");
		ECSAttributeMap map = entity.getComponent(ECSAttributeMap.class);
		if (map == null) {
			return defaultValue;
		}
		ECSAttributeData attrData = map.getAttribute(attribute);
		return attrData == null ? defaultValue : attrData.get();
	}
	
}
