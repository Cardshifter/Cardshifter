package com.cardshifter.modapi.attributes;

import com.cardshifter.modapi.base.Entity;

/**
 * Interface for Attribute types
 */
public interface ECSAttribute {
	/**
	 * Returns the value of this attribute associated with the given entity.
	 * 
	 * @param entity	The entity for which the attribute value is to be retrieved
	 * @return	The attribute value
	 */
    @Deprecated
	default String getFor(final Entity entity) {
		return AttributeRetriever.forAttribute(this).getFor(entity);
	}

    default String getOrDefault(final Entity entity, final String defaultValue) {
        return AttributeRetriever.forAttribute(this).getOrDefault(entity, defaultValue);
    }

}
