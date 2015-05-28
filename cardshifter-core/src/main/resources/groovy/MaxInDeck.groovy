import com.cardshifter.api.config.DeckConfig
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.players.Players
import net.zomis.cardshifter.ecs.config.ConfigComponent

/**
 * Makes it possible to specify card-specific maximums of much of a card that should go into a deck
 */
class MaxInDeck {

    private Map<Integer, Integer> cardCounts = [:]

    def setMaxCardCounts = {e ->
        ConfigComponent config = e.getComponent(ConfigComponent)
        if (config) {
            def deck = config.getConfig(DeckConfig)
            for (def counts in cardCounts) {
                deck.setMax(counts.key, counts.value)
            }
        }
    }

    void initialize(ECSGame game) {
        CardDelegate.metaClass.maxInDeck << {int count ->
            Entity e = entity()
            cardCounts.put(e.id, count)
        }
    }

    void afterConfig(ECSGame game) {
        def players = Players.getPlayersInGame(game)
        players.forEach({e -> setMaxCardCounts.call(e)})
    }

}
