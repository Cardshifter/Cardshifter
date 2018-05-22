package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceBiStrategy
import com.cardshifter.modapi.resources.EntityModifier
import net.zomis.cardshifter.ecs.effects.TargetFilter

import java.util.function.Function
import java.util.function.Predicate

class ModifierDelegate {
    ECSResource resource
    Closure<EntityModifier> modifierCreator

    EntityModifier createModifier(Entity entity) {
        modifierCreator.call(entity)
    }

}

class WhilePresentDelegate {
    List<ModifierDelegate> modifiers = new ArrayList<>()
    List<String> descriptionList = []

    private addModifier(ECSResource[] resources, int priority, Closure filter,
            Function<FilterDelegate, String> stringFunction, ECSResourceBiStrategy strategy) {
        FilterDelegate deleg = new FilterDelegate()
        filter.delegate = deleg
        filter.call()
        Predicate<Entity> active = {!it.isRemoved()}
        TargetFilter appliesTo = deleg.predicate

        Closure<EntityModifier> closure = {Entity entity ->
            new EntityModifier(entity, priority, active, appliesTo, strategy)
        }

        for (ECSResource res : resources) {
            ModifierDelegate modifier = new ModifierDelegate(resource: res, modifierCreator: closure)
            modifiers.add(modifier)
        }

        String desc = stringFunction.apply(deleg)
        descriptionList << desc
    }

    def change(ECSResource... resources) {
        [by: {int change ->
            [withPriority: {int priority ->
                [on: {Closure filter ->
                    addModifier(resources, priority, filter, {deleg ->
                        'give ' + deleg.description + ' ' + change + ' ' + resources.join(' and ')
                    }, {Entity source, Entity target, ECSResource resource, int actualValue ->
                        actualValue + change
                    })
                }]
            }]
        }]
    }

    def set(ECSResource... resources) {
        [to: {int change ->
            [withPriority: {int priority ->
                [on: {Closure filter ->
                    addModifier(resources, priority, filter, { deleg ->
                        'set ' + resources.join(' and ') + ' to ' + deleg.description + ' ' + change
                    }, {Entity source, Entity target, ECSResource resource, int actualValue ->
                          change
                    })
                }]
            }]
        }]
    }

}
