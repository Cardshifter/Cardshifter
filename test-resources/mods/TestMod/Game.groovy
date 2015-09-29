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
TRAMPLE = createResource("TRAMPLE")

PLAY_ACTION = "Play";
ATTACK_ACTION = "Attack";
USE_ACTION = "Use";
ENCHANT_ACTION = "Enchant";

/**
 * Player mana resources.
 */

MANA = createResource("MANA")
MAX_MANA = createResource("MAX_MANA")

/**
 * Which Groovy files to include for this mod. See extra-resources/mods for details.
 */
include 'creatures'
include 'enchantment'
include 'scrap'
include 'noAttack'
include 'spells'

onCard('creature') {entity, args ->
    // give all creatures taunt by default
    entity.taunt = 1
}

onCard('#after') {entity ->
    imagePath 'img'
}

/**
 * General game configuration
 */

config {
    println 'Game Closure!'

    neutral {
        resourceModifier()
        phases()
        zone('Cards') {
            /**
             * List of cardsets to load into the game entity.
             * Note: The 'Cards' zone contains all cards available to the game entity.
             */
            cardset 'cards'
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
        // initializing players' mana and scrap, which are set in the "rules" method in this file
        mana 0
        scrap 0
    }
}

rules {
    init {
        game.players.each {
            it.deck.createFromConfig('Deck') // create play deck from each player's Deck Builder...
            it.deck.shuffle()                // ...shuffle it
            it.drawCards(5)                  // ...draw "n" cards each
        }
        mulliganIndividual()                 // allow mulligan at start of game.
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

        effectAction()       // perform an effect associated with the card

        perform {  // perform after played:
            card.moveTo 'Battlefield' // move played card onto Battlefield
        }
    }

    action('Attack') {
        allowFor {              // only allow if...
            ownedBy 'active'    // ...card is owned by active player
            zone 'Battlefield'  // ...card is present on Battlefield
        }
        requires {                      // requiring...
            require card.sickness == 0  // ...no sickness
        }
        targets 1 of {          // number of allowed targets...
            ownedBy 'opponent'  // ...owned by the opponent
        }

        cost ATTACK_AVAILABLE value 1 on { card }  // depletes "n" ATTACK_AVAILABLE on attacking card

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

            trample(TRAMPLE, HEALTH) // ...ATTACK in excess of defender's HEALTH roll-over to the opponent player's HEALTH.
        }

        perform {  // perform after attacking:
            if (card.deny_counterattack > 0) { // if attacker denies counterattack...
                card.sickness = 2              // ...they have sickness next turn
            }
        }
    }

    turnStart {  // perform on start of turn:
        if (event.oldPhase.owner != null) {  // if it's not the first turn of the game...
            you.drawCard()                   // ...draw a card
        }
        // 1)  your max_mana is the lesser of...
        // 2) ...1 + your max_mana
        // 3) ... or 10, the maximum possible mana
        you.max_mana = Math.min(1 + (int) you.max_mana, 10)
        you.mana = you.max_mana      // current mana is set to max_mana

        you.battlefield.forEach {    // for each card on your Battlefield...
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
        // CYBORG-CHRONICLES MECHANIC:
        you.battlefield.forEach {      // for each of your cards on Battlefield...
            it.health = it.max_health  // ...restore their health to their max_health
        }
        you.opponent.battlefield.forEach {  // for each of your cards on Battlefield...
            it.health = it.max_health       // ...restore their health to their max_health
        }
    }

    action('Enchant') {
        allowFor {             // only allow if...
            ownedBy 'active'   // ...Enchantment card is owned by active player
            zone 'Hand'        // ...Enchantment card is on hand
        }

        cardTargetFilter()    // with card-defined target filter(s)
        effectAction()        // perform an effect associated with the card

        requireTarget {
            it.target.creatureType?.has('Bio')
        }

        // 1) this action costs MANA to play
        // 2) the value it costs is equal to mana_cost value of the card
        // 3) card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }

        perform {
            it.destroy() // destroy the Enchantment card after use
        }
    }

    action('Use') {           // Card which have a Use action (e.g., spells, etc.)
        allowFor {            // only allow if...
            ownedBy 'active'  // ...card is owned by active player
            zone 'Hand'       // ...card is on hand
        }
        cardTargetFilter()    // with card-defined target filter(s)
        effectAction()        // perform an effect associated with the card

        // 1) this action costs MANA to play
        // 2) the value it costs is equal to mana_cost value of the card
        // 3) card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }

        perform {
            it.destroy()      // destroy the card after use
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
/*
setup {
    playerDeckFromConfig('Deck')
    playerDeckShuffle()

    systems {
        gainResource(res: MAX_MANA, value: 1, untilMax: 10)
        restoreResources(resource: MANA, value: MAX_MANA)

        // Play
        playFromHand PLAY_ACTION
        useCost(action: PLAY_ACTION, res: MANA, value: MANA_COST, whoPays: "player")
        playEntersBattlefield PLAY_ACTION

        // Enchant
        playFromHand ENCHANT_ACTION
        EnchantTargetCreatureTypes('Bio')
        EffectActionSystem(ENCHANT_ACTION) // needs to be before EnchantPerform, because of entity removal
        EnchantPerform(ATTACK, HEALTH, MAX_HEALTH)

        // Spell
        useCost(action: USE_ACTION, res: MANA, value: MANA_COST, whoPays: "player")
        playFromHand USE_ACTION
        EffectActionSystem(USE_ACTION)
        EffectActionSystem(PLAY_ACTION)
        targetFilterSystem USE_ACTION
        effectOnSummon 'Battlefield'
        destroyAfterUse USE_ACTION

        RestoreResourcesToSystem(filter: ownedBattlefieldCreatures, resource: ATTACK_AVAILABLE, value: 1)
        RestoreResourcesToSystem(filter: ownedBattlefieldCreatures, resource: SICKNESS,
                value: {ent -> Math.max(0, (int) SICKNESS.getFor(ent) - 1)})

        // Draw cards
        startCards 5
        DrawCardAtBeginningOfTurnSystem()
        DamageConstantWhenOutOfCardsSystem(HEALTH, 1)
        LimitedHandSizeSystem(10, {card -> card.getCardToDraw().destroy()})

        // General setup
        MulliganSingleCards(game)
        GameOverIfNoHealth(HEALTH)
        LastPlayersStandingEndsGame()
        RemoveDeadEntityFromZoneSystem()
        PerformerMustBeCurrentPlayer()
        removeDead(HEALTH)
        ResourceRecountSystem()

        def allowCounterAttack = {attacker, defender ->
            return attacker.deny_counterattack == 0;
        }

        attackSystem {
            zone 'Battlefield'
            cardsFirst TAUNT
            sickness SICKNESS
            useCost(action: ATTACK_ACTION, res: ATTACK_AVAILABLE, value: 1, whoPays: "self")
            accumulating(ATTACK, HEALTH, allowCounterAttack)
            healAtEndOfTurn(HEALTH, MAX_HEALTH)
            afterAttack({entity -> entity.deny_counterattack > 0},
                    { entity -> SICKNESS.retriever.set(entity, 2) })
            trample HEALTH
        }
    }
}
*/
