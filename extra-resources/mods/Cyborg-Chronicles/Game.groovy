/*
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
include 'enchantment'
include 'scrap'
include 'noAttack'
include 'spells'

// apply on creature cards...
onCard('creature') {entity, args ->
    // ... give all Taunt by default.
    entity.taunt = 1
}

// General game configuration

config {
    println 'Game Closure!'

    neutral {
        resourceModifier()
        phases()
        zone('Cards') {
            /* List of cardsets to load into the game entity.
             * Note: The 'Cards' zone contains all cards available to the game entity. */
            cardset 'mechs'
            cardset 'bios'
            cardset 'enchantments'
            cardset 'spellcards'
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
        // initializing players' mana and scrap, which are set in the "rules" method in this file
        mana 0
        scrap 0
    }
}

// RULES OF THE GAME

rules {

    // Initial config
    init {
        // For each player...
        game.players.each {
            // ...create play deck from Deck Builder
            it.deck.createFromConfig('Deck')
            // ...shuffle it
            it.deck.shuffle()
            // ...draw 5 cards
            it.drawCards(5)
        }
        // Allow mulligan at start of game
        mulliganIndividual()
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
        perform {
            // if attacker denies counterattack...
            if (card.deny_counterattack > 0) {
                // ...they have sickness next turn
                card.sickness = 2
            }
        }
    }

    // Define how a turn starts
    turnStart {
        // if it's not the first turn of the game...
        if (event.oldPhase.owner != null) {
            // ...draw a card
            you.drawCard()
        }
        /* 1)  your mana_max is the lesser of...
         * 2) ...1 + your mana_max
         * 3) ... or 10, the maximum possible mana */
        you.mana_max = Math.min(1 + (int) you.mana_max, 10)
        // current mana is set to mana_max
        you.mana = you.mana_max
        // for each card on your Battlefield...
        you.battlefield.forEach {
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
        // CYBORG-CHRONICLES MECHANIC:
        // for each of your cards on Battlefield...
        you.battlefield.forEach {
            // ...restore their health to their max_health
            it.health = it.max_health
        }
        // for each of your opponent's cards on Battlefield...
        you.opponent.battlefield.forEach {
            // ...restore their health to their max_health
            it.health = it.max_health
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
            // CYBORG-CHRONICLES MECHANIC: ...of type Bio
            creatureType 'Bio'
            // ...owned by you
            ownedBy 'you'
        }

        /* 1) this action costs MANA to play
         * 2) the value it costs is equal to mana_cost value of the card
         * 3) card.owner indicates that the card's owner should pay this cost */
        cost MANA value { card.mana_cost } on { card.owner }
        // perform an effect associated with the card
        effectAction()
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
        // perform an effect associated with the card
        effectAction()

        // 1) this action costs MANA to play
        // 2) the value it costs is equal to mana_cost value of the card
        // 3) card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }

        // Perform actions on Use:
        perform {
            // destroy the card after use
            it.destroy()
        }
    }


     /* GAME CONSTANTS
      * Most of these do not require changes as they are elementary functions for the game to work properly. */
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
/*
setup {
    playerDeckFromConfig('Deck')
    playerDeckShuffle()

    systems {
        gainResource(res: MANA_MAX, value: 1, untilMax: 10)
        restoreResources(resource: MANA, value: MANA_MAX)

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
