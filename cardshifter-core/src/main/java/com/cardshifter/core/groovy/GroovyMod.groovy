package com.cardshifter.core.groovy

import com.cardshifter.api.config.DeckConfig
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.events.EntityCreatedEvent
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceDefault
import groovy.transform.PackageScope
import net.zomis.cardshifter.ecs.config.ConfigComponent
import org.codehaus.groovy.control.CompilerConfiguration;
import static groovy.lang.Closure.DELEGATE_ONLY;

public class GroovyMod {

    File modDirectory
    ECSGame game
    ScriptRunner scriptRunner
    final CardDelegate cardDelegate = new CardDelegate(mod: this)
    private List<Closure> configClosure = []
    private List<Closure> setupClosure = []
    private List<Closure> rulesClosure = []
    private Map<String, ECSResource> knownResources = [:]
    @PackageScope Map<String, List<Closure>> cardMethodListeners = [:]

    @Deprecated
    ECSResource createResource(String name) {
        def res = new ECSResourceDefault(name)
        knownResources.put(res.toString().toUpperCase(), res)
        return res
    }

    void onCard(String method, Closure closure) {
        cardMethodListeners.putIfAbsent(method, new ArrayList<Closure>())
        cardMethodListeners[method].add(closure)
    }

    File findFile(String fileName) {
        if (!fileName.contains('.')) {
            fileName += '.groovy'
        }
        File[] files = [
            new File(modDirectory, fileName),
            new File(modDirectory.getParentFile(), fileName),
            new File('mods/' + fileName),
            new File(fileName),
        ]
        Arrays.stream(files)
            .filter({f -> f.exists()})
            .findFirst()
            .orElseThrow({
                String basePath = new File('').getAbsolutePath()
                String arrayInfo = Arrays.toString(files)
                new IllegalArgumentException("Unable to find file: $fileName . Searched in: $arrayInfo . Base path is $basePath")
            })
    }

    void cardExtension(String name, Closure closure) {
        cardDelegate.extMethod(name, closure)
    }

    void include(String fileName) {
        File file = findFile(fileName)
        scriptRunner.runScript(file, this)
        println "Included $fileName"
    }

    ECSResource resource(String name) {
        ECSResource result = resourceOrNull(name)
        assert result : "Resource with name $name not found. Known resources is $knownResources"
        result
    }

    ECSResource resourceOrNull(String name) {
        knownResources[name.toUpperCase()]
    }

    private static def enableMeta(ECSGame game) {
        GeneralSystems.setup(game)
        GeneralSystems.cardSystems(game)
        GeneralSystems.resourceSystems()
    }

    void declareConfiguration(ECSGame game) {
        this.game = game
        def entityMeta = new ExpandoMetaClass(Entity, false, true)
        game.metaClass.getEntityMeta << {
            entityMeta
        }
        game.metaClass.getPlayers << {
            Players.getPlayersInGame(delegate)
        }
        game.metaClass.resource << {String resourceName ->
            resource(resourceName)
        }
        println "Known resources is $knownResources"
        knownResources.forEach {key, value ->
            def lowerCaseKey = key.toLowerCase().capitalize()
            game.entityMeta."get$lowerCaseKey" << {
                value.retriever.getOrDefault(delegate, 0)
            }
            game.entityMeta."set$lowerCaseKey" << {int newValue ->
                value.retriever.set(delegate, newValue)
            }
        }
        entityMeta.initialize()
        this.game.getEvents().registerHandlerAfter(this, EntityCreatedEvent, {e ->
            e.entity.setMetaClass(game.entityMeta)
        })
        enableMeta(game)
        def confDelegate = new ConfigDelegate(game: game, mod: this, cardDelegate: cardDelegate)

        MaxInDeck maxInDeck = new MaxInDeck()
        maxInDeck.initialize(game)
        configClosure.each {
            def cl = it.rehydrate(confDelegate, it.owner, it.thisObject)
            cl.setResolveStrategy(Closure.DELEGATE_FIRST)
            cl.call()
        }
        maxInDeck.afterConfig(game)
    }

    void setupGame(ECSGame game) {
        this.game = game
        def setupDelegate = new SetupDelegate(this, game)
        setupClosure.each {
            def cl = it.rehydrate(setupDelegate, it.owner, it.thisObject)
            cl.setResolveStrategy(Closure.DELEGATE_FIRST)
            cl.call()
        }

        def rulesDelegate = new RulesDelegate(this, game)
        rulesClosure.each {
            def cl = it.rehydrate(rulesDelegate, it.owner, it.thisObject)
            cl.setResolveStrategy(Closure.DELEGATE_FIRST)
            cl.call()
        }
    }

    @Deprecated
    void setup(Closure<?> closure) {
        this.setupClosure << closure
    }

    void config(Closure<?> closure) {
        println 'Config ' + closure
        this.configClosure << closure
    }

    void rules(Closure<?> closure) {
        println 'Rules'
        this.rulesClosure << closure
        println 'Added rule closure to list'
    }
}

class ConfigDelegate {
    ECSGame game
    GroovyMod mod
    CardDelegate cardDelegate

    def resources(List<ECSResource> resources) {
        mod.resources(resources)
    }

    def neutral(Closure closure) {
        closure.delegate = new NeutralDelegate(entity: game.newEntity(), mod: mod, cardDelegate: cardDelegate)
        closure.call()
    }

    def players(int count, Closure closure) {
        for (int i = 0; i < count; i++) {
            Entity player = game.newEntity()
            def cl = closure.rehydrate(new PlayerDelegate(player, mod), this, this)
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
        this.mod = mod
        this.game = game
    }

    def playerDeckFromConfig(String name) {
        def players = Players.getPlayersInGame(game)
        for (Entity player in players) {
            DeckComponent deck = player.getComponent(DeckComponent)
            ConfigComponent playerConfig = player.getComponent(ConfigComponent)
            DeckConfig config = (DeckConfig) playerConfig.getConfigs().get(name)
            setupDeck(deck, config)
        }
    }

    static def setupDeck(DeckComponent deck, DeckConfig deckConf) {
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

    void systems(Closure<?> closure) {
        def cl = closure.rehydrate(new SystemsDelegate(game: game), closure.owner, closure.thisObject);
        cl.call()
    }
}
