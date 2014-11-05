package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Entity;

/**
 * Interface for Resource types
 */
public interface ECSResource {
	default int getFor(final Entity entity) {
		return ResourceRetriever.forResource(this).getFor(entity);
	}
}
