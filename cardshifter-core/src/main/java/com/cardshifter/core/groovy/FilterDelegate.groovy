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

    protected List<GroovyFilter> filters = []

    static FilterDelegate fromClosure(Closure closure) {
        FilterDelegate filter = new FilterDelegate()
        def filterClosure = closure.rehydrate(filter, closure.owner, closure.thisObject)
        filterClosure.call()
        assert !filter.isEmpty() : 'Filter cannot be empty'
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

    def ownedBy(String player) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                if (player == 'you') {
                    return Players.findOwnerFor(source) == Players.findOwnerFor(target)
                }
                if (player == 'opponent') {
                    def targetOwner = Players.findOwnerFor(target)
                    return Players.findOwnerFor(source) != targetOwner && targetOwner != null
                }
                if (player == 'current player' || player == 'active') {
                    return ComponentRetriever.singleton(source.game, PhaseController).currentEntity ==
                            Players.findOwnerFor(target)
                }
                if (player == 'inactive player') {
                    return ComponentRetriever.singleton(source.game, PhaseController).currentEntity !=
                            Players.findOwnerFor(target)
                }
                assert false : 'Unknown owner string: ' + player
            } as TargetFilter,
            description: "owned by $player"
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
            } as TargetFilter,
            description: 'creatures of type ' + String.join(' or ', type)
        ))
    }

    def zone(String... zone) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                CardComponent cardComponent = target.getComponent(CardComponent)
                Cards.isCard(target) && cardComponent.getCurrentZone() && cardComponent.getCurrentZone().getName() in zone
            } as TargetFilter,
            description: 'on ' + String.join(' or ', zone)
        ))
    }

    def creature(boolean creature) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                CreatureTypeComponent creatureType = target.getComponent(CreatureTypeComponent)
                return creatureType != null
            } as TargetFilter,
            description: creature ? 'creatures' : 'non-creatures'
        ))
    }

    def player(boolean player) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                PlayerComponent comp = target.getComponent(PlayerComponent)
                return comp != null
            } as TargetFilter,
            description: player ? 'players' : 'non-players'
        ))
    }

    def cardName(String... name) {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                target.name in name
            } as TargetFilter,
            description: 'cards with name \'' + String.join("'/'", name) + '\''
        ))
    }

    def thisCard() {
        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                source == target
            } as TargetFilter,
            description: 'this card'
        ))
    }

    def not(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        FilterDelegate deleg = fromClosure(closure)

        filters.add(new GroovyFilter(
            predicate: {Entity source, Entity target ->
                !deleg.predicate.test(source, target)
            } as TargetFilter,
            description: deleg.filters.description.stream()
                    .map({description -> "not $description"})
                    .collect(Collectors.joining(' '))
        ))
    }

}
