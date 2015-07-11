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
// FOR COMPATIBILITY TESTING
SCRAP = createResource("SCRAP")
// Cost of Scrap resource to the player for casting the card into play
SCRAP_COST = createResource("SCRAP_COST")

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
include 'scrap'
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
            //cardset 'hindu'
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

setup {

    // Add selected cards to player deck.
    playerDeckFromConfig('Deck')

    // Shuffle the cards in the decks. If you disable this, cards will not be shuffled
    // and instead be in the same order each time the deck is loaded.
    playerDeckShuffle()

    /**
     * THIS "systems" SECTION IS VERY IMPACTING TO HOW THE GAME PLAYS!
     * PLEASE READ THE DOCUMENTATION CAREFULLY!
     * This section is in the works to be improved and made easier to use, see this issue:
     * https://github.com/Cardshifter/Cardshifter/issues/247
     */

    systems {
        /**
         * Defines how player resources are handled at the beginning of the game and as turns go by.
         * @param res The resource to be gained.
         * @param value The initial value on the first turn of each player.
         * @param untilMax The maximum value that the resource will reach.
         */
        gainResource(res: MANA_MAX, value: 10, untilMax: 100)

        /**
         * Defines the resource to be restored at the beginning of each turn.
         * @param resource The resource to be restored.
         * @param value The value to restore the resource to.
         */
        restoreResources(resource: MANA, value: MANA_MAX)

        /**
         * Declares upkeep cost of cards.
         * Upkeep cost is defined as the "tax" on a resource of having a card in a zone.
         * Generally used for creatures that you own on the Battlefield, but can be used for other things.
         * If the mod does not use this mechanic, this can be disabled.
         * @param filter Refers to the name of the method/closure that filters cards.
         * @param decreaseBy Refers to the value by which to decrease the resource.
         * @param decrease Refers to which resource is being decreased by "decreaseBy" param.
         */
        upkeepCost(filter: ownedBattlefieldCreatures, decreaseBy: MANA_COST, decrease: MANA)

        /**
         * "PLAY" ACTION DEFINITION
         */
        // Declares the play action
        playFromHand PLAY_ACTION
        useCost(action: PLAY_ACTION, res: MANA, value: MANA_COST, whoPays: "player")
        playEntersBattlefield PLAY_ACTION

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

        def allowCounterAttackRes = DENY_COUNTERATTACK.retriever;
        def allowCounterAttack = {attacker, defender ->
            return allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
        }

        attackSystem {
            zone 'Battlefield'
            cardsFirst TAUNT
            sickness SICKNESS
            useCost(action: ATTACK_ACTION, res: ATTACK_AVAILABLE, value: 1, whoPays: "self")
            accumulating(ATTACK, HEALTH, allowCounterAttack)
            afterAttack({entity -> allowCounterAttackRes.getFor(entity) > 0},
                    { entity -> SICKNESS.retriever.set(entity, 2) })
            trample HEALTH
        }
    }
}
