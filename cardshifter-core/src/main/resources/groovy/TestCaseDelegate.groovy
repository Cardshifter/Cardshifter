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
            entity = performer
        }
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
        if (expectedOK) {
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
    }

}

class TestCaseDelegate {
    ECSGame game

    private ActionChain active = new ActionChain(delegate: this)

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

    ActionChain uses(String action) {
        active.init()
        active.uses(action)
        return active
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
            assert you
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

}