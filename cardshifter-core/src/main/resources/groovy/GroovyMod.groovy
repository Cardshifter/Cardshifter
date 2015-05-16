import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;

class NeutralDelegate {
    Entity entity

    def resourceModifier() {
        entity.addComponent(new com.cardshifter.modapi.resources.ResourceModifierComponent());
    }

    def zone(String name, Closure<?> closure) {
        println "Zone $name $closure"
    }
    def addCards() {
        println 'Add cards'
    }
}

class PlayersDelegate {
    def config(Closure<?> closure) {
        println "Config closure"
    }
    def deck(Closure<?> closure) {

    }
    def minSize(int value) {}
    def maxSize(int value) {}
    def minCardsPerType(int value) {}
    def zone(String value) {}
}

public abstract class GroovyMod implements ECSMod {

    protected ECSGame game
    abstract def setup()
    abstract def config()

    def enableMeta() {
        ECSGame.class.metaClass.neutral << {Closure closure ->
            println 'Neutral closure'
            def cl = closure.rehydrate(new NeutralDelegate(entity: game.newEntity()), null, null)
            cl.call()
        }
        ECSGame.class.metaClass.players << {int count, Closure closure ->
            println 'Players closure'
            def cl = closure.rehydrate(new PlayersDelegate(), null, null)
            cl.call()
        }
    }

    void declareConfiguration(ECSGame game) {
        enableMeta()
        this.game = game
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