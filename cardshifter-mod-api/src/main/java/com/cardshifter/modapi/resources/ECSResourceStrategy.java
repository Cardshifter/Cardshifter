package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Entity;

@FunctionalInterface
public interface ECSResourceStrategy {
	int getResource(Entity entity, int actualValue);
}
