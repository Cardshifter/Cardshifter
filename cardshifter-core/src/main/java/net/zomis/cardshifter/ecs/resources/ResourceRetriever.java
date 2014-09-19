package net.zomis.cardshifter.ecs.resources;

import java.util.Objects;

import net.zomis.cardshifter.ecs.base.Entity;

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
		ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
		return Objects.requireNonNull(map, entity + " does not have a resource component");
	}

	public ECSResourceData resFor(Entity entity) {
		return resMap(entity).getResource(resource);
	}

}
