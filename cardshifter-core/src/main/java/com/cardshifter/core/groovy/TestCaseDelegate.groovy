package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.actions.ECSAction
import com.cardshifter.modapi.actions.TargetSet
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.Retrievers
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.players.Players

class ActionChain {
    TestCaseDelegate delegate
    private boolean active = false
    private boolean performs = true

    Entity performer
    Entity entity
    String actionName
    List<Entity> targets
    boolean expectedOK = true
    def init() {
        assert !active : 'Previous action was not terminated. Did you forget to call `ok`?'
        active = true
    }

    def propertyMissing(String name) {
        assert active : 'Action is not initialized'
        if (name == 'ok') {
            perform()
            active = false
            return;
        }
        throw new MissingPropertyException(name, ActionChain)
    }

    ActionChain uses(String actionName) {
        assert this.actionName == null
        this.actionName = actionName
        return this
    }

    ActionChain allows(String actionName) {
        this.performs = false
        return uses(actionName)
    }

    ActionChain withTargets(List<Entity> list) {
        assert targets == null
        this.targets = new ArrayList<>(list)
        return this
    }

    ActionChain withTarget(Entity target) {
        return withTargets([target])
    }

    ActionChain withTargets(int targets) {
        assert targets == 0
        return withTargets([])
    }

    ActionChain entity(Entity who) {
        assert this.entity == null
        this.entity = who
        return this
    }

    ActionChain on(Entity who) {
        return entity(who)
    }

    List<Entity> getAvailableTargets() {
        assert entity : 'No entity specified'
        def results = this.entity.getComponent(ActionComponent).getAction(actionName)
            .getTargetSets().get(0).findPossibleTargets()
        clear()
        active = false
        results
    }

    ActionChain by(Entity performer) {
        assert this.performer == null
        this.performer = performer
        return this
    }

    private void perform() {
        if (!performer) {
            performer = delegate.currentPlayer()
        }
        if (!entity) {
            println "entity is $entity, setting to performer $performer"
            entity = performer
        }
        println 'Perform ' + this
        ActionComponent actions = entity.getComponent(ActionComponent)
        ECSAction action = actions.getAction(actionName)
        assert action : "Action with name $actionName not found on entity ${Entity.debugInfo(entity)}"
        if (targets) {
            assert action.isAllowed(performer)
            assert action.getTargetSets().size() == 1
            TargetSet targetSet = action.getTargetSets().get(0)
            List<Entity> failedTargets = []
            for (Entity target : targets) {
                if (!targetSet.addTarget(target)) {
                    failedTargets << target
                }
            }
            assert failedTargets.isEmpty() == expectedOK
        } else {
            assert action.isAllowed(performer) == expectedOK
        }
        if (expectedOK && performs) {
            assert action.perform(performer) == expectedOK
        }
        clear()
    }

    private void clear() {
        this.performer = null
        this.entity = null
        this.actionName = null
        this.targets = null
        this.expectedOK = true
        this.performs = true
    }

    boolean isActive() {
        return active
    }

    String toString() {
        String performVerb = performs ? 'performs' : 'checks if allowed'
        return "ActionChain[$active] entity $entity $performVerb $actionName by $performer with targets $targets expected $expectedOK"
    }

}

class TestCaseDelegate {
    ECSGame game
    CardDelegate cardDelegate
    GroovyMod mod

    private final ActionChain active = new ActionChain(delegate: this)

    ActionChain entity(Entity entity) {
        active.init()
        active.entity(entity)
    }

    def expect(boolean expectedOK) {
        active.init()
        active.expectedOK = expectedOK
        return [when: {Entity who ->
            active.entity(who)
        }]
    }

    boolean finished() {
        !active.isActive()
    }

    ActionChain uses(String action) {
        active.init()
        active.uses(action)
        return active
    }

    ActionChain allowed(String action) {
        active.init()
        active.allows(action)
    }

    ActionChain withTargets(List<Entity> targets) {
        active.init()
        active.withTargets(targets)
        return active
    }

    ActionChain withTarget(Entity target) {
        active.init()
        active.withTargets([target])
        return active
    }

    Entity currentPlayer() {
        PhaseController phaseController = Retrievers.singleton(game, PhaseController)
        return phaseController.currentEntity
    }

    def propertyMissing(String name) {
        if (name == 'failure') {
            return false
        }
        if (name == 'ok') {
            return true
        }
        if (name == 'you') {
            Entity you = currentPlayer()
            assert you : 'There is no active player'
            return you
        }
        if (name == 'currentPlayer') {
            return currentPlayer()
        }
        if (name == 'opponent') {
            Entity you = currentPlayer()
            assert Players.getNextPlayer(you)
            return Players.getNextPlayer(you)
        }
        throw new MissingPropertyException(name, ActionChain)
    }

    def to(Entity who) {
        [zone: {String zoneName ->
            [create: {Object card ->
                if (card instanceof Closure) {
                    assert card
                    assert card.thisObject
                    def zone = EffectDelegate.zoneLookup(who, zoneName)
                    def closure = card
                    closure.delegate = cardDelegate
                    def entity = cardDelegate.createCard(game.newEntity(), closure, Closure.OWNER_FIRST)
                    zone.addOnBottom(entity)
                    return entity
                } else if (card instanceof String) {
                    def entity = EffectDelegate.cardModelByName(game, card as String)
                    assert entity
                    def zone = EffectDelegate.zoneLookup(who, zoneName)
                    def created = entity.copy()
                    zone.addOnBottom(created)
                    return created
                } else {
                    throw new RuntimeException('need to specify a String (card name) or Closure')
                }
            }]
        }]
    }

}