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
	default String getFor(final Entity entity) {
		return AttributeRetriever.forAttribute(this).getFor(entity);
	}
}
