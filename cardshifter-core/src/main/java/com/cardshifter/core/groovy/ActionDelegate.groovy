package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent
import com.cardshifter.modapi.actions.ActionPerformEvent
import com.cardshifter.modapi.actions.UseCostSystem
import com.cardshifter.modapi.actions.attack.AttackDamageAccumulating
import com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn
import com.cardshifter.modapi.actions.attack.AttackSickness
import com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer
import com.cardshifter.modapi.actions.attack.TrampleSystem
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.resources.ECSResource
import groovy.transform.Immutable
import groovy.transform.PackageScope
import net.zomis.cardshifter.ecs.effects.EffectActionSystem
import net.zomis.cardshifter.ecs.effects.EffectTargetFilterSystem
import net.zomis.cardshifter.ecs.usage.ApplyAfterAttack

import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.ToIntFunction
import java.util.function.UnaryOperator

/**
 * Created by Simon on 6/24/2015.
 */
class ActionDelegate {
    private final String name
    private final ECSGame game

    ActionDelegate(ECSGame game, String name) {
        this.name = name
        this.game = game
    }
    final String opponent = 'inactive player'

    void allowFor(Closure closure) {
        def filter = FilterDelegate.fromClosure{closure}
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent, {
                    it.setAllowed(filter.predicate.test(it.entity, it.entity))
                })
            }
        })
    }

    void requires(Closure closure) {
        def delegate = new RequiresDelegate()
        def requirements = closure.rehydrate(delegate, closure.owner, closure.thisObject)
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent, {
                    delegate.setup(it)
                    requirements.call(it)
                    it.setAllowed(delegate.allowed)
                })
            }
        })
    }

    private static class RequiresDelegate {
        Entity card
        Entity performer
        @PackageScope boolean allowed

        @PackageScope void setup(ActionAllowedCheckEvent event) {
            this.card = event.entity
            this.performer = event.performer
            this.allowed = event.allowed
        }

        void require(boolean bool) {
            allowed = allowed && bool
        }
    }

    Map targets(int count) {
        [of: {Closure closure ->
            FilterDelegate filter = FilterDelegate.fromClosure closure

        }]
    }

    private static class CardHolderDelegate {
        final Entity card

        CardHolderDelegate(Entity card) {
            this.card = card
        }
    }

    Map cost(ECSResource resource) {
        [value: {Object value ->
            ToIntFunction<Entity> costFunction
            if (value instanceof Integer) {
                costFunction = {e -> value as Integer}
            } else if (value instanceof Closure) {
                costFunction = {e ->
                    def clos = value as Closure
                    def deleg = new CardHolderDelegate(e)
                    Closure cl = clos.rehydrate(deleg, clos.owner, clos.thisObject)
                    cl.call(e)
                }
            }
            [on: {Closure<Entity> closure ->
                UnaryOperator<Entity> whoPays = {Entity e ->
                    def deleg = new CardHolderDelegate(e)
                    Closure<Entity> cl = closure.rehydrate(deleg, closure.owner, closure.thisObject)
                    cl.call(e)
                } as UnaryOperator<Entity>
                game.addSystem(new UseCostSystem(name, resource, costFunction, whoPays))
            }]
        }]
    }

    void perform(Closure closure) {
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.getEvents().registerHandlerAfter(this, ActionPerformEvent, {
                    def performClosure = closure.rehydrate(new CardHolderDelegate(it.entity), closure.owner, closure.thisObject)
                    performClosure.call(it)
                })
            }
        })
    }

    void cardTargetFilter() {
        game.addSystem new EffectTargetFilterSystem(name)
    }

    void effectAction() {
        game.addSystem new EffectActionSystem(name)
    }

    void attack(Closure closure) {
        def delegate = new AttackDelegate(game: game)
        closure.setDelegate(delegate)
        closure.call()
    }

    private static class AttackDelegate {
        ECSGame game

        void accumulating(ECSResource attack, ECSResource health, BiPredicate<Entity, Entity> allowCounterAttack) {
            game.addSystem new AttackDamageAccumulating(attack, health, allowCounterAttack)
        }

        void trample(ECSResource trample, ECSResource resource) {
            game.addSystem new TrampleSystem(trample, resource)
        }

        void battlefieldFirst(ECSResource resource) {
            game.addSystem new AttackTargetMinionsFirstThenPlayer(resource)
        }

    }

}
