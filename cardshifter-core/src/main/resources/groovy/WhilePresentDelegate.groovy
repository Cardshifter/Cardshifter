import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.CreatureTypeComponent
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.CardComponent
import com.cardshifter.modapi.cards.Cards
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceBiStrategy
import com.cardshifter.modapi.resources.EntityModifier

import java.util.function.BiPredicate
import java.util.function.Function
import java.util.function.Predicate

class ModifierDelegate {
    ECSResource resource
    Closure<EntityModifier> modifierCreator

    EntityModifier createModifier(Entity entity) {
        modifierCreator.call(entity)
    }

}

class FilterDelegate {
    BiPredicate<Entity, Entity> predicate = {Entity source, Entity target -> true}
    StringBuilder description = new StringBuilder()

    private void addAnd() {
        if (description.length() > 0) {
            description.append(' and ')
        }
    }

    def ownedBy(String owner) {
        addAnd()
        description.append("owned by $owner")
        predicate = predicate.and({Entity source, Entity target ->
            if (owner == 'you') {
                return Players.findOwnerFor(source) == Players.findOwnerFor(target)
            }
            if (owner == 'opponent') {
                return Players.findOwnerFor(source) == Players.findOwnerFor(target)
            }
            if (owner == 'current player') {
                return ComponentRetriever.singleton(source.game, PhaseController).currentEntity ==
                        Players.findOwnerFor(target)
            }
            if (owner == 'inactive player') {
                return ComponentRetriever.singleton(source.game, PhaseController).currentEntity !=
                        Players.findOwnerFor(target)
            }
            assert false : 'Unknown owner string: ' + owner
        })
    }

    def creatureType(String... type) {
        addAnd()
        description.append('creatures of type ' + String.join(' or ', type))
        predicate = predicate.and({Entity source, Entity target ->
            CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
            if (creatureType) {
                return creatureType.getCreatureType() in type
            } else {
                return false
            }
        })
    }

    def zone(String... zone) {
        addAnd()
        description.append('on ' + String.join(' or ', zone))
        predicate = predicate.and({Entity source, Entity target ->
            CardComponent cardComponent = target.getComponent(CardComponent)
            Cards.isCard(target) && cardComponent.getCurrentZone() && cardComponent.getCurrentZone().getName() in zone
        })
    }

    def creature(boolean creature) {
        addAnd()
        description.append(creature ? 'creatures' : 'non-creatures')
        predicate = predicate.and({Entity source, Entity target ->
            CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
            return creatureType != null
        })
    }

    def cardName(String... name) {
        addAnd()
        description.append('cards with name \'' + String.join("'/'", name) + '\'')
        predicate = predicate.and({Entity source, Entity target ->
            target.name in name
        })
    }

    def thisCard() {
        addAnd()
        description.append('this card')
        predicate = predicate.and({Entity source, Entity target ->
            source == target
        })
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
        BiPredicate<Entity, Entity> appliesTo = deleg.predicate

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
                [onCards: {Closure filter ->
                    addModifier(resources, priority, filter, {deleg ->
                        'Give ' + deleg.description + ' ' + change + ' ' + resources.join(' and ')
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
                [onCards: {Closure filter ->
                    addModifier(resources, priority, filter, { deleg ->
                        'Set ' + resources.join(' and ') + ' to ' + deleg.description + ' ' + change
                    }, {Entity source, Entity target, ECSResource resource, int actualValue ->
                          change
                    })
                }]
            }]
        }]
    }

}