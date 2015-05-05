package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Entity;

public interface ECSResourceBiStrategy {
    int getResource(Entity source, Entity target, ECSResource resource, int actualValue);
}