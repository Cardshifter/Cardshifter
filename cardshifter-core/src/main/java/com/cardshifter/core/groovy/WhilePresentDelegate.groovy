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
    StringBuilder description = new StringBuilder()

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
        description.append(desc)
        description.append('\n')
    }

    def change(ECSResource... resources) {
        [by: {int change ->
            [withPriority: {int priority ->
                [on: {Closure filter ->
                    addModifier(resources, priority, filter, {deleg ->
                        String changeStr
                        if (change == 0) {
                            changeStr = 'unchanged'
                        }
                        else {
                            changeStr = [change.abs(), change < 0 ? 'less' : 'more'].join(' ')
                        }

                        [deleg.description.capitalize(), 'have', changeStr, resources.join(' and ')].join(' ')
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
                        [deleg.description.capitalize(), 'have', resources.join(' and '), 'set to', change].join(' ')
                    }, {Entity source, Entity target, ECSResource resource, int actualValue ->
                          change
                    })
                }]
            }]
        }]
    }

}
