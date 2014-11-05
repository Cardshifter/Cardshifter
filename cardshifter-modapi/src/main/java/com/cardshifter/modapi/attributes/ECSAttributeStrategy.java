package com.cardshifter.modapi.attributes;

import com.cardshifter.modapi.base.Entity;

@FunctionalInterface
public interface ECSAttributeStrategy {
	String getAttribute(Entity entity, String actualValue);
}
