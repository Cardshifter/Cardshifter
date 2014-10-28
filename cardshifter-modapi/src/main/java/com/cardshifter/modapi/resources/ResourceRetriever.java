package com.cardshifter.modapi.resources;

import java.util.Objects;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;

public class ResourceRetriever {

	private final ECSResource resource;

	ResourceRetriever(ECSResource resource) {
		this.resource = resource;
	}
	
	public static ResourceRetriever forResource(ECSResource resource) {
		return new ResourceRetriever(resource);
	}

	public int getFor(Entity entity) {
		return resMap(entity).getResource(resource).get();
	}

	private ECSResourceMap resMap(Entity entity) {
		Objects.requireNonNull(entity, "Cannot retrieve resource map for null entity");
		if (entity.isRemoved()) {
			throw new IllegalArgumentException(entity + " has been marked for removal.");
		}
		ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
		return Objects.requireNonNull(map, entity + " does not have a resource component: " + entity.getSuperComponents(Component.class));
	}
	
	public boolean has(Entity entity) {
		Objects.requireNonNull(entity, "Cannot retrieve resource map for null entity");
		ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
		return map != null && resFor(entity) != null;
	}

	public ECSResourceData resFor(Entity entity) {
		return resMap(entity).getResource(resource);
	}

	public ECSResource getResource() {
		return resource;
	}

	public int getOrDefault(Entity entity, int defaultValue) {
		Objects.requireNonNull(entity, "Cannot retrieve resource map for null entity");
		ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
		if (map == null) {
			return defaultValue;
		}
		ECSResourceData resData = map.getResource(resource);
		return resData == null ? defaultValue : resData.get();
	}
	
}
