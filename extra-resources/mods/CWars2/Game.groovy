/*
 * The Game class sets up all the parameters upon a game being started for a mod using the client.
 * It is imported during run time, hence errors with the mod will be logged to the server console at run time.
 * @author Simon Forsberg [code]
 * @author Francis Gaboury [docs]
 */
import com.cardshifter.modapi.actions.*

// Resources related to cards

CASTLE = createResource('CASTLE')
WALL = createResource("WALL")
ATTACK = createResource("ATTACK")
HANDSIZE = createResource("HANDSIZE")
DISCARDS = createResource("DISCARDS")

BRICKS = createResource("BRICKS")
WEAPONS = createResource("WEAPONS")
CRYSTALS = createResource("CRYSTALS")

BUILDERS = createResource("BUILDERS")
RECRUITERS = createResource("RECRUITERS")
WIZARDS = createResource("WIZARDS")

// Actions that are related to cards, defined in more detail further down this file.

PLAY_ACTION = "Play"

/* Which Groovy files to include for this mod.
 * See extra-resources/mods for details. */

include 'spells'

onCard('#after') {entity ->
    if (!entity.imagePath) {
        println 'Adding default image path for ' + entity.name
        imagePath 'cyborg-chronicles/default.png'
    }
    def actions = entity.getComponent(ActionComponent)
    def playAction = new ECSAction(entity, 'Play', {act -> true }, {act -> })
    actions.addAction(playAction)
}

// General game configuration

config {
    println 'Game Closure! (CWars2)'

    neutral {
        resourceModifier()
        phases()
        /**
         * List of cardsets to load into the game entity.
         * <p>
         * Note: The 'Cards' zone contains all cards available to the game entity.
         */
        zone('Cards') {
            cardset 'brick_cards'
            cardset 'weapon_cards'
        }
    }

    /**
     * Player configurations
     * <p>
     * Creates identical configuration for each player. As of current time (2015-08-11),
     * only 2-player play is supported.
     * This entry will be updated if or when this changes.
     */
    players(2) {
        phase 'Main'
        config {
            /**
             * General deck configuration.
             *
             * @param minSize  Minimum number of cards allowed in a deck
             * @param maxSize  Maximum number of cards allowed in a deck
             * @param zone  Name of the zone for cards, default 'Cards'
             * @param maxCardsPerType  Default maximum of any given card in a deck,
             *   except those for which this value is overridden by
             *   the optional maxInDeck property available for any card.
             */
            deck {
                minSize 10
                maxSize 750
                maxCardsPerType 5
                zone 'Cards'
            }
        }
        hand()
        discard()
        /**
         * Players' resources at the very beginning of a game.
         * <p>
         *     These resources are applied before any actions are allowed
         *     to either player by the server. These resources can and do get
         *     modified during the course of a game, which various depending
         *     on a game mod's configuration.
         * <p>
         *     See each mod's documentation and game rules for details.
         */
        castle 25
        wall 15
        handsize 8
        discards 3

        bricks 8
        weapons 8
        crystals 8

        builders 2
        recruiters 2
        wizards 2
    }

}

// RULES OF THE GAME

rules {

    /*
    * TODO: First turn should not give resources
    */

    // Initial config at game start
    init {
        game.players.each {
            it.deck.createFromConfig('Deck')
            it.deck.shuffle()
            it.drawCards(8)
        }
    }

    discardMulliganAction(1, 3)

    /* Define the action of playing a card. */
    action('Play') {
        /**
         * Which cards are allowed to be played.
         *
         * @param ownedBy  The owner player of the playable card,
         *      default 'active' (i.e., the player whose turn it currently is)
         * @param zone  The zone where the playable card must be prior to
         *  being played, default 'Hand'
         */
        allowFor {
            ownedBy 'active'
            zone 'Hand'
        }
        /* 1) this action costs stuff to play
         * 2) the value it costs is equal to a value of the card
         * 3) card.owner indicates that the card's owner should pay this cost */
        cost BRICKS value { card.bricks } on { card.owner }
        cost WEAPONS value { card.weapons } on { card.owner }
        cost CRYSTALS value { card.crystals } on { card.owner }
        // perform an effect associated with the card
        effectAction()
        // Perform when Played:
        perform {
            endTurn()
        }
    }

    // Define how a turn starts
    turnStart {
        you.bricks += you.builders
        you.weapons += you.recruiters
        you.crystals += you.wizards
    }

    // GAME CONSTANTS

    //Most of these do not require changes as they are elementary functions for the game to work properly. */
    always {
        // When player is out of cards to draw, reshuffle deck
        reshuffleWhenOutOfCardsSystem()

        // Game ends if at any time a player has no castle or has 100 castle
        GameOverIfNo(CASTLE)
        winIfTarget(CASTLE, 100)

        // If one player loses, the other one wins
        LastPlayersStandingEndsGame()

        // Actions can only be performed by the player whose turn it is, a.k.a. 'active'
        PerformerMustBeCurrentPlayer()

        // Attack wall first, then castle
        attackEnemy(ATTACK, WALL, CASTLE)

        // Resource system
        ResourceRecountSystem()
    }
}
