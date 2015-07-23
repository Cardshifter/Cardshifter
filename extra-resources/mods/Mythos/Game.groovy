/**
 * The Game class sets up all the parameters upon a game being started for a mod using the client.
 * It is imported during run time, hence errors with the mod will be logged to the server console at run time.
 * @author Simon Forsberg
 */

/**
 * @param entity A card entity.
 * @return Boolean value indicating whether or not the entity is a creature, is on battlefield, and is owned by the current player.
 */
def ownedBattlefieldCreatures = {entity ->
    def Cards = com.cardshifter.modapi.cards.Cards;
    return entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class) &&
            Cards.isOnZone(entity, com.cardshifter.modapi.cards.BattlefieldComponent.class) &&
            Cards.isOwnedByCurrentPlayer(entity)
}

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
        // initializing players' mana and mana max, which are set in the "setup" method in this file
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

        perform {
            card.moveTo 'Battlefield' // play card onto Battlefield
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
            ownedBy opponent    // ...owned by the opponent
        }

        cost ATTACK_AVAILABLE value 1 on { card } // depletes "n" ATTACK_AVAILABLE on attacking card

        attack {                    // allow attack on...
            battlefieldFirst TAUNT  // ...only creatures with TAUNT first, if present
            def allowCounterAttack = {attacker, defender -> attacker.deny_counterattack == 0 } // ...defender counterattacks on attacker, if attacker cannot deny it
            accumulating(ATTACK, HEALTH, allowCounterAttack)
            trample(TRAMPLE, HEALTH)
        }

        perform {
            if (card.deny_counterattack > 0) {
                card.sickness = 2
            }
        }
    }

    turnStart('Main') {
        if (event.oldPhase.owner != null) {
            you.drawCard()
        }
        you.mana_max = Math.min(10 + (int) you.mana_max, 100)
        you.mana = you.mana_max
        you.battlefield.forEach {
            you.mana -= it.mana_cost
            it.attack_available = 1
            if (it.sickness > 0) {
                it.sickness--
            }
        }
    }

    turnEnd {
        if (you == null) {
            return;
        }
/*        you.battlefield.forEach {
            it.health = it.max_health
        }*/
    }

    action('Enchant') {
        allowFor {
            ownedBy 'active'
            zone 'Hand'
        }
        targets 1 of {
            zone 'Battlefield'
            creatureType 'Bio'
            ownedBy 'you'
        }

        cost MANA value { card.mana_cost } on { card.owner }
        effectAction()
        perform {
            targets.forEach {
                it.attack += card.attack
                it.health += card.health
                it.max_health += card.health
            }
            it.destroy()
        }
    }

    action('Use') {
        allowFor {
            ownedBy 'active'
            zone 'Hand'
        }
        cardTargetFilter()

        cost MANA value { card.mana_cost } on { card.owner }

        perform {
            effectAction()
            destroy()
        }
    }

    always {
        effectOnSummon 'Battlefield'
        limitedHandSize(10, {card -> card.getCardToDraw().destroy()})
        DamageConstantWhenOutOfCardsSystem(HEALTH, 1)

        GameOverIfNo(HEALTH)
        LastPlayersStandingEndsGame()
        removeDead()
        PerformerMustBeCurrentPlayer()
        removeDead(HEALTH)
        ResourceRecountSystem()
    }
}
