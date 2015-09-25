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

import java.util.stream.Collectors

class FilterDelegate {

    private List<GroovyFilter> filters = []

    static FilterDelegate fromClosure(Closure closure) {
        FilterDelegate filter = new FilterDelegate()
        def filterClosure = closure.rehydrate(filter, closure.owner, closure.thisObject)
        filterClosure.call()
        assert !filter.isEmpty() : 'Filter cannot be empty.'
        return filter
    }

    List<Entity> findMatching(Entity source) {
        source.game.findEntities({Entity e ->
            predicate.test(source, e)
        })
    }

    TargetFilter getPredicate() {
        filters.stream()
               .inject(GroovyFilter.getIdentity().predicate) {result, element -> result.and(element.predicate)}
    }

    String getDescription() {
        filters.stream()
               .map({filter -> filter.description})
               .collect(Collectors.joining(' '))
    }

    boolean isEmpty() {
        filters.isEmpty()
    }

    def ownedBy(String owner) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
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
            },
            description: "owned by $owner"
        ))
    }

    def creatureType(String... type) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
                if (creatureType) {
                    return creatureType.hasAny(type)
                } else {
                    return false
                }
            },
            description: 'creatures of type ' + String.join(' or ', type)
        ))
    }

    def zone(String... zone) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                CardComponent cardComponent = target.getComponent(CardComponent)
                Cards.isCard(target) && cardComponent.getCurrentZone() && cardComponent.getCurrentZone().getName() in zone
            },
            description: 'on ' + String.join(' or ', zone)
        ))
    }

    def creature(boolean creature) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
                return creatureType != null
            },
            description: creature ? 'creatures' : 'non-creatures'
        ))
    }

    def player(boolean player) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                PlayerComponent comp = target.getComponent(PlayerComponent)
                return comp != null
            },
            description: player ? 'players' : 'non-players'
        ))
    }

    def cardName(String... name) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                target.name in name
            },
            description: 'cards with name \'' + String.join("'/'", name) + '\''
        ))
    }

    def thisCard() {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                source == target
            },
            description: 'this card'
        ))
    }

}
