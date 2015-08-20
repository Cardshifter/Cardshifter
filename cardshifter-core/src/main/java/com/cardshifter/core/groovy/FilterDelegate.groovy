package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.CreatureTypeComponent
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.CardComponent
import com.cardshifter.modapi.cards.Cards
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.players.Players
import net.zomis.cardshifter.ecs.effects.TargetFilter

class FilterDelegate {
    TargetFilter predicate = {Entity source, Entity target -> true}
    StringBuilder description = new StringBuilder()

    static FilterDelegate fromClosure(Closure closure) {
        FilterDelegate filter = new FilterDelegate()
        def filterClosure = closure.rehydrate(filter, closure.owner, closure.thisObject)
        filterClosure.call()
        assert !filter.description.toString().isEmpty() : 'Empty filter detected.'
        return filter
    }

    List<Entity> findMatching(Entity source) {
        source.game.findEntities({Entity e ->
            predicate.test(source, e)
        })
    }

    private void addAnd() {
        if (description.length() > 0) {
            description.append(' ')
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
                def targetOwner = Players.findOwnerFor(target)
                return Players.findOwnerFor(source) != targetOwner && targetOwner != null
            }
            if (owner == 'current player' || owner == 'active') {
                return ComponentRetriever.singleton(source.game, PhaseController).currentEntity ==
                        Players.findOwnerFor(target)
            }
            if (owner == 'inactive player') {
                return ComponentRetriever.singleton(source.game, PhaseController).currentEntity !=
                        Players.findOwnerFor(target)
            }
            assert false : 'Unknown owner string: ' + owner
        } as TargetFilter)
    }

    def creatureType(String... type) {
        addAnd()
        description.append('creatures of type ' + String.join(' or ', type))
        predicate = predicate.and({Entity source, Entity target ->
            CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
            if (creatureType) {
                return creatureType.hasAny(type)
            } else {
                return false
            }
        } as TargetFilter)
    }

    def zone(String... zone) {
        addAnd()
        description.append('on ' + String.join(' or ', zone))
        predicate = predicate.and({Entity source, Entity target ->
            CardComponent cardComponent = target.getComponent(CardComponent)
            Cards.isCard(target) && cardComponent.getCurrentZone() && cardComponent.getCurrentZone().getName() in zone
        } as TargetFilter)
    }

    def creature(boolean creature) {
        addAnd()
        description.append(creature ? 'creatures' : 'non-creatures')
        predicate = predicate.and({Entity source, Entity target ->
            CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
            return creatureType != null
        } as TargetFilter)
    }

    def player(boolean player) {
        addAnd()
        description.append(player ? 'players' : 'non-players')
        predicate = predicate.and({Entity source, Entity target ->
            PlayerComponent comp = target.getComponent(PlayerComponent)
            return comp != null
        } as TargetFilter)
    }

    def cardName(String... name) {
        addAnd()
        description.append('cards with name \'' + String.join("'/'", name) + '\'')
        predicate = predicate.and({Entity source, Entity target ->
            target.name in name
        } as TargetFilter)
    }

    def thisCard() {
        addAnd()
        description.append('this card')
        predicate = predicate.and({Entity source, Entity target ->
            source == target
        } as TargetFilter)
    }

}
