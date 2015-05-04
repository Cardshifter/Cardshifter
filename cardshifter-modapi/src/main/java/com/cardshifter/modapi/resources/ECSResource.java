package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Entity;

/**
 * Interface for Resource types
 */
public interface ECSResource {
	/**
	 * Returns the value of this resource associated with the given entity.
	 * 
	 * @param entity	The entity for which the resource value is to be retrieved
	 * @return	The resource value
	 */
	default int getFor(final Entity entity) {
		return ResourceRetriever.forResource(this).getFor(entity);
	}

    /**
     * Create a resource retriever for this resource
     *
     * @return a resource retriever for this resource
     */
    default ResourceRetriever retriever() {
        return ResourceRetriever.forResource(this);
    }

    /**
     * alias for {@link #retriever()}
     * @return see {@link #retriever()}
     */
    default ResourceRetriever getRetriever() {
        return retriever();
    }
}
