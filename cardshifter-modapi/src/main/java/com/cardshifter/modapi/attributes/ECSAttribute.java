package com.cardshifter.modapi.attributes;

import com.cardshifter.modapi.base.Entity;

/**
 * Interface for Attribute types
 */
public interface ECSAttribute {
	default String getFor(final Entity entity) {
		return AttributeRetriever.forAttribute(this).getFor(entity);
	}
}
