package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionPerformEvent
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.cards.Cards
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource

class AttackEnemySystem implements ECSSystem {
    final ECSResource attackResource
    final ECSResource[] defenses

    AttackEnemySystem(ECSResource attackResource, ECSResource... defenses) {
        this.attackResource = attackResource
        this.defenses = defenses
    }

    @Override
    void startGame(ECSGame game) {
        game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, {e ->
            def owner = Cards.getOwner(e.entity)
            def opponent = Players.getNextPlayer(owner)
            int attack = attackResource.getFor(e.entity)
            for (ECSResource resource : defenses) {
                def oppDefense = resource.getFor(opponent)
                int currentDamage = Math.min(oppDefense, attack)
                resource.retriever().resFor(opponent).changeBy(-currentDamage, {i -> i})
                attack -= currentDamage
                if (attack <= 0) {
                    return
                }
            }
        })
    }
}
