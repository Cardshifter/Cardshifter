package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.Entity;

@FunctionalInterface
public interface EntityInt {
    int valueFor(Entity entity);
}
