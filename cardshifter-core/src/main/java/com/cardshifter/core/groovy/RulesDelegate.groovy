package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.cards.MulliganSingleCards
import com.cardshifter.modapi.phase.PhaseChangeEvent

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Created by Simon on 6/24/2015.
 */
class RulesDelegate {
    final GroovyMod mod
    final ECSGame game

    def RulesDelegate(GroovyMod mod, ECSGame game) {
        this.mod = mod
        this.game = game
    }

    private static void callWithDelegate(Closure closure, EventYouDelegate delegate) {
        closure.setDelegate(delegate)
        closure.setResolveStrategy(DELEGATE_FIRST)
        closure.call()
    }

    void init(Closure closure) {
        closure.setDelegate(this)
        closure.call()
    }

    void mulliganIndividual() {
        game.addSystem(new MulliganSingleCards(game))
    }

    void onStart(Closure closure) {
        game.addSystem({game ->
            closure.setDelegate(this)
            closure.setResolveStrategy(DELEGATE_FIRST)
            closure.call()
        })
    }

    void action(String name, Closure closure) {
        closure.setDelegate(new ActionDelegate(name))
        closure.setResolveStrategy(DELEGATE_FIRST)
        closure.call()
    }

    void turnStart(Closure closure) {
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.events.registerHandlerAfter(this, PhaseChangeEvent, {event ->
                    callWithDelegate(closure, new EventYouDelegate(event))
                })
            }
        })
    }

    void turnEnd(Closure closure) {
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.events.registerHandlerBefore(this, PhaseChangeEvent, {event ->
                    callWithDelegate(closure, new EventYouDelegate(event))
                })
            }
        })
    }

    void turnStart(String name, Closure closure) {
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.events.registerHandlerAfter(this, PhaseChangeEvent, {event ->
                    if (name == event.newPhase.name) {
                        callWithDelegate(closure, new EventYouDelegate(event))
                    }
                })
            }
        })
    }

    void turnEnd(String name, Closure closure) {
        game.addSystem(new ECSSystem() {
            @Override
            void startGame(ECSGame game) {
                game.events.registerHandlerBefore(this, PhaseChangeEvent, {event ->
                    if (name == event.oldPhase.name) {
                        callWithDelegate(closure, new EventYouDelegate(event))
                    }
                })
            }
        })
    }

    void always(Closure closure) {
        def cl = closure.rehydrate(new SystemsDelegate(game: game), closure.owner, closure.thisObject);
        cl.call()
    }

}
