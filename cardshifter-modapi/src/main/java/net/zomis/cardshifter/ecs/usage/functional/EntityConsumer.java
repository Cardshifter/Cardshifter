package net.zomis.cardshifter.ecs.usage.functional;

import com.cardshifter.modapi.base.Entity;

@FunctionalInterface
public interface EntityConsumer {
    void perform(Entity source, Entity target);
}
