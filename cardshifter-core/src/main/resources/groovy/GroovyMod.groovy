import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.phase.Phase
import com.cardshifter.modapi.phase.PhaseController
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceDefault
import com.cardshifter.modapi.resources.ECSResourceMap
import com.cardshifter.modapi.resources.ResourceModifierComponent
import net.zomis.cardshifter.ecs.config.ConfigComponent
import net.zomis.cardshifter.ecs.config.DeckConfigFactory;

class CardDelegate {
    Entity entity

    def propertyMissing(String name, value) {
        println "Missing property: Cannot set $name to $value"
        ECSResource res = entity.game.resource(name)
        if (res) {
            res.retriever.set(entity, (int) value)
        }
    }

    def propertyMissing(String name) {
        println 'Missing property: ' + name
    }

    def methodMissing(String name, args) {
        println 'Missing method: ' + name
        ECSResource res = entity.game.resource(name)
        if (res) {
            res.retriever.set(entity, (int) args[0])
        }
    }
}

class ZoneDelegate {
    Entity entity
    ZoneComponent zone

    def cards(Closure<?> closure) {
        closure.delegate = this
        closure.call()
    }

    def card(String name, Closure<?> closure) {
        def card = entity.game.newEntity()
        ECSAttributeMap.createFor(card).set(Attributes.NAME, name)
        ECSResourceMap.createFor(card)
        closure.delegate = new CardDelegate(entity: card)
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure.call()
        zone.addOnBottom(card)
    }

    def card(Closure<?> closure) {
        card('', closure)
    }
}

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
    }

    def phase(String name) {
        def controller = ComponentRetriever.singleton(entity.game, PhaseController)
        controller.addPhase(new Phase(entity, name))
    }

    def config(Closure<?> closure) {
        println "Config closure"
        closure.setDelegate(this)
        closure.call()
    }

    def deck(@DelegatesTo(DeckDelegate) Closure<?> closure) {
        def deckConfig = new DeckDelegate()
        closure.delegate = deckConfig
        closure.call()
        List cardList = entity.game.findEntities({en -> en.hasComponent(ZoneComponent)})
        assert cardList.size() == 1
        ZoneComponent zone = cardList.get(0).getComponent(ZoneComponent)
        assert zone.name == deckConfig.zoneName
        def deck = DeckConfigFactory.create(deckConfig.minSize, deckConfig.maxSize, zone.cards, deckConfig.maxCardsPerType)
        config.addConfig("Deck", deck)
    }
}

public abstract class GroovyMod implements ECSMod {

    protected ECSGame game
    abstract def setup()
    abstract def config()

    ECSResource createResource(String name) {
        return new ECSResourceDefault(name)
    }

    void resources(List<ECSResource> resources) {
        def map = [:]
        for (ECSResource res in resources) {
            map.put(res.toString().toUpperCase(), res)
        }
        game.metaClass.resource << {String name -> map[name.toUpperCase()]}
    }

    private def enableMeta() {
        ECSGame.class.metaClass.neutral << {Closure closure ->
            def cl = closure.rehydrate(new NeutralDelegate(entity: game.newEntity()), this, this)
            cl.call()
        }
        ECSGame.class.metaClass.players << {int count, Closure closure ->
            for (int i = 0; i < count; i++) {
                Entity player = game.newEntity()
                def cl = closure.rehydrate(new PlayerDelegate(player), null, null)
                player.addComponent(new PlayerComponent(i, "Player $i"))
                cl.call()
            }
        }
    }

    void declareConfiguration(ECSGame game) {
        this.game = game
        enableMeta()
        config()
    }

    void setupGame(ECSGame game) {
        this.game = game
        setup()
    }

    void game(Closure<?> closure) {
        def cl = closure.rehydrate(game, this, this)
        cl.call()
    }

}