package net.zomis.cardshifter.ecs.resources;

import net.zomis.cardshifter.ecs.base.Entity;

public class ResourceRetreiver {

	private final ECSResource resource;

	private ResourceRetreiver(ECSResource resource) {
		this.resource = resource;
	}
	
	public static ResourceRetreiver forResource(ECSResource resource) {
		return new ResourceRetreiver(resource);
	}

	public int getFor(Entity entity) {
		return entity.getComponent(ECSResourceMap.class).getResource(resource).get();
	}

	public ECSResourceData resFor(Entity entity) {
		return entity.getComponent(ECSResourceMap.class).getResource(resource);
	}

}
