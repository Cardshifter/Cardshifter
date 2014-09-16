package net.zomis.cardshifter.ecs.resources;

import net.zomis.cardshifter.ecs.base.Entity;

@FunctionalInterface
public interface ECSResourceStrategy {
	int getResource(Entity entity, int actualValue);
}
