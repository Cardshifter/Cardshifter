import com.cardshifter.api.config.DeckConfig
import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.actions.ECSAction
import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.BattlefieldComponent
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.cards.HandComponent
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.phase.Phase
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceDefault
import com.cardshifter.modapi.resources.ResourceModifierComponent
import net.zomis.cardshifter.ecs.config.ConfigComponent
import net.zomis.cardshifter.ecs.config.DeckConfigFactory;
import SystemsDelegate;
import ZoneDelegate;
import systems.GeneralSystems;

class NeutralDelegate {
    Entity entity

    def resourceModifier() {
        entity.addComponent(new ResourceModifierComponent());
    }

    def phases() {
        entity.addComponent(new PhaseController())
    }

    def zone(String name, Closure<?> closure) {
        def zone = new ZoneComponent(entity, name)
        entity.addComponent(zone)
        println "Zone $name"
        closure.delegate = new ZoneDelegate(entity: entity, zone: zone)
        closure.call(closure)
    }
    def addCards() {
        println 'Add cards'
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

class PlayerDelegate {
    Entity entity
    def config = new ConfigComponent()

    PlayerDelegate(Entity entity) {
        this.entity = entity
        entity.addComponent(config);
        entity.addComponent(new ActionComponent())
    }

    def methodMissing(String name, args) {
        ECSResource res = entity.game.resource(name)
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

public class GroovyMod {

    protected ECSGame game

    ECSResource createResource(String name) {
        return new ECSResourceDefault(name)
    }

    void resources(List<ECSResource> resources) {
        println "Adding ${resources.size()} resources"
        def map = [:]
        for (ECSResource res in resources) {
            map.put(res.toString().toUpperCase(), res)
        }
        game.metaClass.resource << {String name -> map[name.toUpperCase()]}
    }

    private def enableMeta() {
        GeneralSystems.setup()
        GeneralSystems.cardSystems()
        ECSGame.class.metaClass.neutral << {Closure closure ->
            def cl = closure.rehydrate(new NeutralDelegate(entity: game.newEntity()), this, this)
            cl.call()
        }
        ECSGame.class.metaClass.players << {int count, Closure closure ->
            for (int i = 0; i < count; i++) {
                println 'Creating player ' + i
                Entity player = game.newEntity()
                def cl = closure.rehydrate(new PlayerDelegate(player), this, this)
                player.addComponent(new PlayerComponent(i, "Player $i"))
                cl.setResolveStrategy(Closure.DELEGATE_FIRST)
                cl.call()
            }
        }
    }

    private Closure<?> configClosure
    private Closure<?> setupClosure

    void declareConfiguration(ECSGame game) {
        this.game = game
        enableMeta()
        def cl = configClosure.rehydrate(game, this, this)
        cl.setResolveStrategy(Closure.DELEGATE_FIRST)
        cl.call()
    }

    void setupGame(ECSGame game) {
        this.game = game
        println 'Creating setup delegate'
        def cl = setupClosure.rehydrate(new SetupDelegate(this, game), this, this)
        cl.setResolveStrategy(Closure.DELEGATE_ONLY)
        cl.call()
    }

    void setup(Closure<?> closure) {
        this.setupClosure = closure
    }

    void config(Closure<?> closure) {
        this.configClosure = closure
    }

}

class SetupDelegate {
    def mod
    ECSGame game

    SetupDelegate(GroovyMod mod, ECSGame game) {
        println 'Creating setup delegate 2'
        this.mod = mod
        this.game = game
    }

    def playerDeckFromConfig(String name) {
        println "Player deck from config $name"
        def players = Players.getPlayersInGame(game)
        for (Entity player in players) {
            DeckComponent deck = player.getComponent(DeckComponent)
            ConfigComponent playerConfig = player.getComponent(ConfigComponent)
            DeckConfig config = playerConfig.getConfigs().get(name)
            setupDeck(deck, config)
        }
    }

    def setupDeck(DeckComponent deck, DeckConfig deckConf) {
        def game = deck.owner.game;
        for (def chosen in deckConf.chosen.entrySet()) {
            def entityId = chosen.key;
            def count = chosen.value;

            for (int i = 0; i < count; i++) {
                def existing = game.getEntity(entityId);
                def copy = existing.copy();
                deck.addOnBottom(copy);
            }
        }
    }

    def playerDeckShuffle() {
        def players = Players.getPlayersInGame(game)
        for (Entity player in players) {
            DeckComponent deck = player.getComponent(DeckComponent)
            deck.shuffle()
        }
    }

    def methodMissing(String name, args) {
        println 'Setup Unsupported method: ' + name
    }

    void systems(Closure<?> closure) {
        def cl = closure.rehydrate(new SystemsDelegate(game: game), this, this);
        cl.call()
    }
}
