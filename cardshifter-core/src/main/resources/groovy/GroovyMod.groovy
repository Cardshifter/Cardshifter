import com.cardshifter.api.config.DeckConfig
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceDefault
import net.zomis.cardshifter.ecs.config.ConfigComponent
import SystemsDelegate;
import PlayerDelegate;
import NeutralDelegate;
import systems.GeneralSystems;
import static groovy.lang.Closure.DELEGATE_ONLY;

public class GroovyMod {

    ClassLoader loader
    File modDirectory
    ECSGame game
    private List<Closure> configClosure = []
    private List<Closure> setupClosure = []

    ECSResource createResource(String name) {
        return new ECSResourceDefault(name)
    }

    void include(String fileName) {
        GroovyShell sh = new GroovyShell(loader, new Binding())
        Object script = sh.run(new File("groovy/${fileName}.groovy"), [])
        println "Include $fileName resulted in $script"
    }

    void resources(List<ECSResource> resources) {
        println "Adding ${resources.size()} resources"
        def map = [:]
        for (ECSResource res in resources) {
            map.put(res.toString().toUpperCase(), res)
        }
        game.metaClass.resource << {String name -> map[name.toUpperCase()]}
    }

    private static def enableMeta(ECSGame game) {
        GeneralSystems.setup(game)
        GeneralSystems.cardSystems(game)
        GeneralSystems.resourceSystems()
    }

    void declareConfiguration(ECSGame game) {
        this.game = game
        enableMeta(game)
        def confDelegate = new ConfigDelegate(game: game, mod: this)
        configClosure.each {
            def cl = it.rehydrate(confDelegate, this, this)
            cl.setResolveStrategy(DELEGATE_ONLY)
            cl.call()
        }
    }

    void setupGame(ECSGame game) {
        this.game = game
        println 'Creating setup delegate'
        def setupDelegate = new SetupDelegate(this, game)
        setupClosure.each {
            def cl = it.rehydrate(setupDelegate, this, this)
            cl.setResolveStrategy(DELEGATE_ONLY)
            cl.call()
        }
    }

    void setup(Closure<?> closure) {
        this.setupClosure << closure
    }

    void config(Closure<?> closure) {
        this.configClosure << closure
    }
}

class ConfigDelegate {
    ECSGame game
    GroovyMod mod

    def resources(List<ECSResource> resources) {
        mod.resources(resources)
    }

    def neutral(Closure closure) {
        closure.delegate = new NeutralDelegate(entity: game.newEntity(), mod: mod)
        closure.call()
    }

    def players(int count, Closure closure) {
        for (int i = 0; i < count; i++) {
            println 'Creating player ' + i
            Entity player = game.newEntity()
            def cl = closure.rehydrate(new PlayerDelegate(player), this, this)
            player.addComponent(new PlayerComponent(i, "Player $i"))
            cl.setResolveStrategy(DELEGATE_ONLY)
            cl.call()
        }
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
