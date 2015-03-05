package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.Entity;

@FunctionalInterface
public interface TargetInt {
    int value(Entity source, Entity target);
}
