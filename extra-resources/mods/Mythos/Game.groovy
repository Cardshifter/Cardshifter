/**
 * The Game class sets up all the parameters upon a game being started for a mod using the client.
 * It is imported during run time, hence errors with the mod will be logged to the server console at run time.
 * @author Simon Forsberg
 */

/**
 * Resources related to cards
 */

// Amount of damage the card can cause by attacking
ATTACK = createResource('ATTACK')
// Amount of damage the card can suffer before being retired from Battlefield
HEALTH = createResource("HEALTH")
// Maximum HEALTH value that a card may hold
MAX_HEALTH = createResource("MAX_HEALTH")
// Cost of mana resource to the player for casting the card into play
MANA_COST = createResource("MANA_COST")
TRAMPLE = createResource("TRAMPLE")

/**
 * Resources that declare a specific special behavior to creature cards
 **/

// Card cannot attack if set to false
ATTACK_AVAILABLE = createResource("ATTACK_AVAILABLE")
// Card is immune to counter-attack when attacking another creature
DENY_COUNTERATTACK = createResource("DENY_COUNTERATTACK")
// Card cannot attack while not 0
SICKNESS = createResource("SICKNESS")
// Card must be attacked while on Battlefield before the owner player can be attacked
TAUNT = createResource("TAUNT")

/**
 * Actions that are related to cards, defined in more detail further down this file. 
 */

PLAY_ACTION = "Play";
ATTACK_ACTION = "Attack";
USE_ACTION = "Use";
ENCHANT_ACTION = "Enchant";

/**
 * Player mana resources.
 */

MANA = createResource("MANA")
MANA_MAX = createResource("MANA_MAX")


/**
 * Which Groovy files to include for this mod. See extra-resources/mods for details. 
 */
include 'creatures'
include 'noAttack'
include 'enchantment'
// include 'spells' // Bug #324


/**
 * General game configuration
 */

config {
    neutral {
        resourceModifier()
        phases()
        /**
         * List of cardsets to load into the game entity.
         * Note: The 'Cards' zone contains all cards available to the game entity.
         */
        zone('Cards') {
            cardset 'common'
            cardset 'chinese'
            //cardset 'egyptian'
            cardset 'greek'
            //cardset 'norse'
            cardset 'hindu'
            //cardset 'roman'
            //cardset 'nature'
        }
    }

    /**
     * Player configuration
     * Creates identical configuration for each player.
     */

    // Note: As of current time 2015-06-18, only two players are supported. This entry will be updated if/when this changes.
    players(2) {
        phase 'Main'
        config {
            deck {
                // minimum cards in deck
                minSize 30
                // maximum cards in deck
                maxSize 30
                // maximum number of copies of a card
                // can be overridden by the maxInDeck property on individual cards
                maxCardsPerType 3
                zone 'Cards'
            }
        }
        endTurnAction()
        hand()
        battlefield()
        // players' starting health and maximum health
        health 30
        max_health 30
        // initializing players' mana and mana max, which are set in the "rules" method in this file
        mana 0
        mana_max 0
    }
}

rules {
    init {
        mulliganIndividual()                 // allow mulligan at start of game.
        game.players.each {                  // for each player...
            it.deck.createFromConfig('Deck') // ...create play deck from each player's Deck Builder
            it.deck.shuffle()                // ...shuffle it
        }
    }

    onStart {
        game.players.each {
            it.drawCards(5)                  // ...draw "n" cards each
        }
    }

    action('Play') {
        allowFor {           // only allow if...
            ownedBy 'active' // ...card is owned by active player
            zone 'Hand'      // ...card is on hand
        }

        // 1) this action costs MANA to play
        // 2) the value it costs is equal to mana_cost value of the card
        // 3) card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }
        effectAction() // perform an effect associated with the card

        perform {  // perform after play:
            card.moveTo 'Battlefield' // move played card onto Battlefield
        }
    }

    action('Attack') {
        allowFor {              // only allow if...
            ownedBy 'active'    // ...card is owned by active player
            zone 'Battlefield'  // ...card is present on Battlefield
        }
        requires {                     // requiring...
            require card.sickness == 0 // ...no sickness
        }
        targets 1 of {          // number of allowed targets...
            ownedBy 'opponent'  // ...owned by the opponent
        }

        cost ATTACK_AVAILABLE value 1 on { card } // depletes "n" ATTACK_AVAILABLE on attacking card

        attack {                     // allow attack on...
            battlefieldFirst TAUNT   // ...only creatures with TAUNT first, if present
            def allowCounterAttack = {attacker, defender -> attacker.deny_counterattack == 0 } // ...defender counterattacks on attacker, if attacker cannot deny it
            /**
             * This part that is in a way still using the "old system". Attacks are a bit special at the moment.
             * @TODO Refactor to use the new system
             * It means that the attack system is like Hearthstone, that attack is accumulated until a creature dies...
             * ...the amount of damage to deal is specified by the ATTACK resource
             * ...how much health a minion has is specified by HEALTH
             * ...and whether or not they are allowed to counter-attack is specified by the allowCounterAttack closure
             */
            accumulating(ATTACK, HEALTH, allowCounterAttack)

            trample(TRAMPLE, HEALTH) // ...attacker's ATTACK in excess of defender's HEALTH rollover to the defender owner's HEALTH.
        }

        perform {  // perform after attacking:
            if (card.deny_counterattack > 0) { // if attacker denies counterattack...
                card.sickness = 2              // ...they have sickness next turn
            }
        }
    }

    turnStart('Main') {  // perform on start of turn:
        if (event.oldPhase.owner != null) { // if it's not the first turn of the game...
            you.drawCard()                  // ...draw a card
        }
        // 1)  your mana_max is the lesser of...
        // 2) ...10 + your mana_max
        // 3) ... or 100, the maximum possible mana
        you.mana_max = Math.min(10 + (int) you.mana_max, 100)
        you.mana = you.mana_max      // current mana is set to mana_max

        you.battlefield.forEach {    // for each card on your Battlefield...
            // MYTHOS MECHANIC:
            // your mana is reduced by...
            // ...the mana cost of cards on your Battlefield
            you.mana -= it.mana_cost

            it.attack_available = 1  // cards that can attack are set to have an attack available
            if (it.sickness > 0) {   // cards that have any sickness...
                it.sickness -= 1     // ...have their sickness reduced by 1
            }
        }
    }

    turnEnd {  // perform on end of turn:
        if (you == null) {  // if it's the phase before everything starts (i.e., Mulligan phase)...
            return          // ...return from this method without doing anything
        }
    }

    action('Enchant') {
        allowFor {             // only allow if...
            ownedBy 'active'   // ...Enchantment card is owned by active player
            zone 'Hand'        // ...Enchantment card is on hand
        }
        targets 1 of {         // number of allowed targets...
            zone 'Battlefield' // ...present on Battlefield
            ownedBy 'you'      // ...owned by you
        }

        // 1) this action costs MANA to play
        // 2) the value it costs is equal to mana_cost value of the card
        // 3) card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }

        effectAction()  // perform an effect associated with the card

        perform {
            targets.forEach {                // for each target card...
                it.attack += card.attack     // ...add Enchantment's attack to the target's attack
                it.health += card.health     // ...add Enchantment's health to the target's health
                it.max_health += card.health // ...add Enchantment's health to the target's max_health
            }
            it.destroy() // destroy the Enchantment card after use
        }
    }

    action('Use') {           // Card which have a Use action (e.g., spells, etc.)
        allowFor {            // only allow if...
            ownedBy 'active'  // ...card is owned by active player
            zone 'Hand'       // ...card is on hand
        }
        cardTargetFilter()    // with card-defined target filter(s)

        // 1) this action costs MANA to play
        // 2) the value it costs is equal to mana_cost value of the card
        // 3) card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }

        perform {
            effectAction()    // perform an effect associated with the card
            destroy()         // destroy the card after use
        }
    }

    /**
     * GAME CONSTANTS
     * Most of these do not require changes as they are elementary functions for the game to work properly.
     */
    always {
        // Registers a system that listens for when a creature in summoned directly on the battlefield,
        // and performs any associated effects that creature has
        effectOnSummon 'Battlefield'
        // 1) Maximum hand size at any time is 10
        // 2) Cards drawn which are in excess of 10 are destroyed
        limitedHandSize(10, {card -> card.getCardToDraw().destroy()})
        // 1) When player is out of cards to draw...
        // 2) Damage 1 health per turn to player
        DamageConstantWhenOutOfCardsSystem(HEALTH, 1)
        // Game ends if at any time a player has no health
        GameOverIfNo(HEALTH)
        // Last player with health remaining ends the game in a win
        LastPlayersStandingEndsGame()
        // Dead creature cards are removed from the game
        removeDead()
        // Actions can only be performed by the player whose turn it is, a.k.a. 'active'
        PerformerMustBeCurrentPlayer()
        // Cards with no HEALTH are removed
        removeDead(HEALTH)
        // Resource system
        ResourceRecountSystem()
    }
}
