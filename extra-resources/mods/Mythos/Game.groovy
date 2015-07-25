/*
 * The Game class sets up all the parameters upon a game being started for a mod using the client.
 * It is imported during run time, hence errors with the mod will be logged to the server console at run time.
 * @author Simon Forsberg [code]
 * @author Francis Gaboury [docs]
 */

/** Check for Owned Battlefield Creatures
 *  @param entity A card entity.
 *  @return Boolean value indicating whether or not:
 *   - the entity is a creature,
 *   - is on battlefield,
 *   - and is owned by the current player. */
def ownedBattlefieldCreatures = {entity ->
    def Cards = com.cardshifter.modapi.cards.Cards;
    return entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class) &&
            Cards.isOnZone(entity, com.cardshifter.modapi.cards.BattlefieldComponent.class) &&
            Cards.isOwnedByCurrentPlayer(entity)
}

// Resources related to cards

ATTACK = createResource('ATTACK')
HEALTH = createResource("HEALTH")
MAX_HEALTH = createResource("MAX_HEALTH")
MANA_COST = createResource("MANA_COST")

// Resources that declare a specific special behavior to creature cards

ATTACK_AVAILABLE = createResource("ATTACK_AVAILABLE")
DENY_COUNTERATTACK = createResource("DENY_COUNTERATTACK")
SICKNESS = createResource("SICKNESS")
TAUNT = createResource("TAUNT")
TRAMPLE = createResource("TRAMPLE")

// Actions that are related to cards, defined in more detail further down this file.

PLAY_ACTION = "Play";
ATTACK_ACTION = "Attack";
USE_ACTION = "Use";
ENCHANT_ACTION = "Enchant";

// Player mana resources

MANA = createResource("MANA")
MANA_MAX = createResource("MANA_MAX")


/* Which Groovy files to include for this mod.
 * See extra-resources/mods for details. */
include 'creatures'
include 'noAttack'
include 'enchantment'
// include 'spells' // Bug #324

// General game configuration

config {
    neutral {
        resourceModifier()
        phases()
        /* List of cardsets to load into the game entity.
         * Note: The 'Cards' zone contains all cards available to the game entity. */
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

    /* Player configuration
     *  Creates identical configuration for each player. */

    /* Note: As of current time 2015-06-18, only two players are supported.
     *  This entry will be updated if/when this changes. */
    players(2) {
        phase 'Main'
        config {
            deck {
                // minimum cards in deck
                minSize 30
                // maximum cards in deck
                maxSize 30
                /* maximum number of copies of a card
                 *  - can be overridden by the maxInDeck property on individual cards */
                maxCardsPerType 3
                // zone where all available cards are added
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

// RULES OF THE GAME

rules {
    // Initial config
    init {
        // Allow mulligan at start of game
        mulliganIndividual()
        // For each player...
        game.players.each {
            // ...create play deck from Deck Builder
            it.deck.createFromConfig('Deck')
            // ...shuffle it
            it.deck.shuffle()
        }
    }

    onStart {
        game.players.each {
            // ...draw 5 cards
            it.drawCards(5)
        }
    }

    // Define how to Play a card
    action('Play') {
        // only allow if...
        allowFor {
            // ...card is owned by active player
            ownedBy 'active'
            // ...card is on hand
            zone 'Hand'
        }
        /* 1) this action costs MANA to play
         * 2) the value it costs is equal to mana_cost value of the card
         * 3) card.owner indicates that the card's owner should pay this cost */
        cost MANA value { card.mana_cost } on { card.owner }
            // perform an effect associated with the card
            effectAction()

        // Perform when Played:
        perform {
            // ...move played card onto Battlefield */
            card.moveTo 'Battlefield'
        }
    }

    // Define how to Attack with a card
    action('Attack') {
        // only allow if...
        allowFor {
            // ...card is owned by active player
            ownedBy 'active'
            // ...card is present on Battlefield
            zone 'Battlefield'
        }
        // requiring...
        requires {
            // ...no sickness on card
            require card.sickness == 0
        }
        // number of allowed targets...
        targets 1 of {
            // ...owned by the opponent
            ownedBy 'opponent'
        }

        // depletes 1 ATTACK_AVAILABLE on attacking card
        cost ATTACK_AVAILABLE value 1 on { card }

        // Define flow of attack action:
        attack {
            // creatures with TAUNT must be attacked first
            battlefieldFirst TAUNT
            // ...defender counterattacks on attacker, if attacker cannot deny it
            def allowCounterAttack = {attacker, defender -> attacker.deny_counterattack == 0 }

            /* This part that is in a way still using the "old system". Attacks are a bit special at the moment.
             * @TODO Refactor to use the new system
             * It means that the attack system is like Hearthstone, that attack is accumulated until a creature dies...
             * ...the amount of damage to deal is specified by the ATTACK resource
             * ...how much health a minion has is specified by HEALTH
             * ...and whether or not they are allowed to counter-attack is specified by the allowCounterAttack closure
             */
            accumulating(ATTACK, HEALTH, allowCounterAttack)
            // ...ATTACK in excess of defender's HEALTH roll-over to the opponent player's HEALTH.
            trample(TRAMPLE, HEALTH)
        }

        // Perform when attacking:
        perform {  // perform after attacking:
            if (card.deny_counterattack > 0) { // if attacker denies counterattack...
                card.sickness = 2              // ...they have sickness next turn
            }
        }
    }

    // Define how a turn starts
    turnStart('Main') {  // perform on start of turn
        // if it's not the first turn of the game...
        if (event.oldPhase.owner != null) {
            // ...draw a card
            you.drawCard()
        }
        /* 1)  your mana_max is the lesser of...
         * 2) ...1 + your mana_max
         * 3) ... or 10, the maximum possible mana */
        you.mana_max = Math.min(10 + (int) you.mana_max, 100)
        // current mana is set to mana_max
        you.mana = you.mana_max

        // for each card on your Battlefield...
        you.battlefield.forEach {
            /* MYTHOS MECHANIC:
             * your mana is reduced by...
             * ...the mana cost of cards on your Battlefield */
            you.mana -= it.mana_cost
            // ...cards that can attack are set to have an attack available
            it.attack_available = 1
            // cards that have any sickness...
            if (it.sickness > 0) {
                // ...have their sickness reduced by 1
                it.sickness -= 1
            }
        }
    }

    // Define how a turn ends
    turnEnd {
        // if now is the phase before everything starts (i.e., Mulligan phase)...
        if (you == null) {
            // ...return from this method without doing anything
            return
        }
    }

    // Define how to Enchant with a card
    action('Enchant') {
        // only allow if...
        allowFor {
            // ...Enchantment card is owned by active player
            ownedBy 'active'
            // ...Enchantment card is on hand
            zone 'Hand'
        }
        // number of allowed targets...
        targets 1 of {
            // ...present on Battlefield
            zone 'Battlefield'
            // ...owned by you
            ownedBy 'you'
        }

        /* 1) this action costs MANA to play
         * 2) the value it costs is equal to mana_cost value of the card
         * 3) card.owner indicates that the card's owner should pay this cost */
        cost MANA value { card.mana_cost } on { card.owner }
        // perform an effect associated with the card
        effectAction()  // perform an effect associated with the card
        // Perform when Enchanting:
        perform {
            // for each target card...
            targets.forEach {
                // ...add Enchantment's attack to the target's attack
                it.attack += card.attack
                // ...add Enchantment's health to the target's health
                it.health += card.health
                // ...add Enchantment's health to the target's max_health
                it.max_health += card.health
            }
            // destroy the Enchantment card after use
            it.destroy()
        }
    }

    // Define cards with a Use action (e.g., spells, etc.)
    action('Use') {
        // only allow if...
        allowFor {
            // ...card is owned by active player
            ownedBy 'active'
            // ...card is on hand
            zone 'Hand'
        }
        // with card-defined target filter(s)
        cardTargetFilter()

        /* 1) this action costs MANA to play
         * 2) the value it costs is equal to mana_cost value of the card
         * 3) card.owner indicates that the card's owner should pay this cost */
        cost MANA value { card.mana_cost } on { card.owner }

        // Perform actions on Use:
        perform {
            // perform an effect associated with the card
            effectAction()
            // destroy the card after use
            destroy()
        }
    }

    // GAME CONSTANTS

    //Most of these do not require changes as they are elementary functions for the game to work properly.
    always {
        /* Registers a system that listens for when a creature in summoned directly on the battlefield,
         * and performs any associated effects that creature has */
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
