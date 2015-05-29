package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.Entity;
import net.zomis.cardshifter.ecs.effects.TargetFilter;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class EntityModifier implements ECSResourceStrategy {

    private final Entity source;
    private final int priority;
    private final Predicate<Entity> active;
    private final TargetFilter appliesTo;
    private final ECSResourceBiStrategy amount;

    public EntityModifier(Entity entity, int priority, Predicate<Entity> active,
        TargetFilter appliesTo, ECSResourceBiStrategy amount) {
        this.source = entity;
        this.priority = priority;
        this.active = active;
        this.appliesTo = appliesTo;
        this.amount = amount;
    }

    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(Entity target) {
        return appliesTo.test(source, target);
    }

    @Override
    public int getResource(Entity entity, ECSResource resource, int actualValue) {
        return amount.getResource(source, entity, resource, actualValue);
    }

    public boolean isActive() {
        return active.test(source);
    }
}
