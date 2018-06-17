package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionPerformEvent
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.BattlefieldComponent
import com.cardshifter.modapi.cards.DamageConstantWhenOutOfCardsSystem
import com.cardshifter.modapi.cards.DrawCardEvent
import com.cardshifter.modapi.cards.LimitedHandSizeSystem
import com.cardshifter.modapi.cards.RemoveDeadEntityFromZoneSystem
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.GameOverIfTarget
import com.cardshifter.modapi.resources.ResourceRecountSystem
import net.zomis.cardshifter.ecs.usage.LastPlayersStandingEndsGame

import java.util.function.Consumer
import java.util.stream.Collectors

class SystemsDelegate {
    ECSGame game

    void addSystem(ECSSystem system) {
        game.addSystem(system)
    }

    void limitedHandSize(int limit, Consumer<DrawCardEvent> whenFull) {
        addSystem new LimitedHandSizeSystem(limit, whenFull)
    }

    void winIfTarget(ECSResource resource, int target) {
        addSystem new GameOverIfTarget(resource, true, {count -> count >= target})
    }

    void DamageConstantWhenOutOfCardsSystem(ECSResource resource, int count) {
        addSystem new DamageConstantWhenOutOfCardsSystem(resource, count)
    }

    void GameOverIfNo(ECSResource resource) {
        addSystem new GameOverIfTarget(resource, false, {count -> count <= 0})
    }

    void LastPlayersStandingEndsGame() {
        addSystem new LastPlayersStandingEndsGame()
    }

    void removeDead() {
        addSystem new RemoveDeadEntityFromZoneSystem()
    }

    void ResourceRecountSystem() {
        addSystem new ResourceRecountSystem()
    }

    void removeDead(ECSResource resource) {
        addSystem {ECSGame g ->
            g.events.registerHandlerAfter(this, ActionPerformEvent.class, {event ->
                List<Entity> remove = event.entity.game.getEntitiesWithComponent(BattlefieldComponent)
                        .stream().flatMap({entity -> entity.getComponent(BattlefieldComponent).stream()})
                        .peek({Entity e -> println("$e has ${resource.getFor(e)}")})
                        .filter({Entity e -> resource.getFor(e) <= 0})
                        .collect(Collectors.toList());
                for (Entity e in remove) {
                    e.destroy();
                }
            })
        }
    }


}
