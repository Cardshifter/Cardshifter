import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.actions.ECSAction
import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.BattlefieldComponent
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.cards.HandComponent
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.phase.Phase
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.resources.ECSResource
import net.zomis.cardshifter.ecs.config.ConfigComponent
import net.zomis.cardshifter.ecs.config.DeckConfigFactory

public class PlayerDelegate {
    Entity entity
    GroovyMod mod
    def config = new ConfigComponent()

    PlayerDelegate(Entity entity, GroovyMod mod) {
        this.entity = entity
        this.mod = mod
        entity.addComponent(config);
        entity.addComponent(new ActionComponent())
    }

    def methodMissing(String name, args) {
        ECSResource res = mod.resourceOrNull(name)
        if (res) {
            res.retriever.set(entity, (int) args[0])
        } else {
            throw new UnsupportedOperationException("No such method: $name")
        }
    }

    def endTurnAction() {
        PhaseController phaseController = ComponentRetriever.singleton(entity.game, PhaseController)
        entity.getComponent(ActionComponent).addAction(new ECSAction(entity, "End Turn",
                {act -> phaseController.currentPhase.owner == entity},
                {act -> phaseController.nextPhase()}))
    }

    def hand() {
        entity.addComponent(new HandComponent(entity))
    }

    def battlefield() {
        entity.addComponent(new BattlefieldComponent(entity))
    }

    def phase(String name) {
        def controller = ComponentRetriever.singleton(entity.game, PhaseController)
        def phase = new Phase(entity, name)
        controller.addPhase(phase)
        println "Added phase $phase"
    }

    def config(Closure<?> closure) {
        println "Config closure"
        closure.setDelegate(this)
        closure.call()
    }

    def deck(@DelegatesTo(DeckDelegate) Closure<?> closure) {
        println 'Deck config creation'
        def deckConfig = new DeckDelegate()
        closure.delegate = deckConfig
        closure.call()
        List cardList = entity.game.findEntities({en -> en.hasComponent(ZoneComponent)})
        assert cardList.size() == 1
        ZoneComponent zone = cardList.get(0).getComponent(ZoneComponent)
        assert zone.name == deckConfig.zoneName
        def deck = DeckConfigFactory.create(deckConfig.minSize, deckConfig.maxSize, zone.cards, deckConfig.maxCardsPerType)
        config.addConfig("Deck", deck)
        entity.addComponent(new DeckComponent(entity))
    }
}

class DeckDelegate {
    int minSize, maxSize, maxCardsPerType
    String zoneName

    def minSize(int value) {
        this.minSize = value
    }
    def maxSize(int value) {
        this.maxSize = value
    }
    def maxCardsPerType(int value) {
        this.maxCardsPerType = value
    }
    def zone(String value) {
        this.zoneName = value
    }
}
