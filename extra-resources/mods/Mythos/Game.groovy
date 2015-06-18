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

/**
 * Actions that are related to cards, defined in more detail further down this file. 
 */

PLAY_ACTION = "Play";
ATTACK_ACTION = "Attack";
USE_ACTION = "Use";

/**
 * Which Groovy files to include for this mod. See extra-resources/mods for details. 
 */
include 'creatures'
include 'noAttack'

/**
 * General game configuration
 */

config {
    neutral {
        resourceModifier()
        phases()
        /**
         * List of cardsets to load into the Cards zone, which is the pool of all possible cards available.
         */
        zone('Cards') {
            cardset 'common'
            cardset 'chinese'
            //cardset 'egyptian'
            //cardset 'greek'
            //cardset 'norse'
            //cardset 'hindu'
            //cardset 'roman'
        }
    }

    /**
     * General player configuration
     */
    players(2) {
        phase 'Main'
        config {
            deck {
                minSize 30
                maxSize 30
                maxCardsPerType 3
                zone 'Cards'
            }
        }
        endTurnAction()
        hand()
        battlefield()
        health 30
        max_health 30
        mana 0
        mana_max 0
    }
}

setup {
    playerDeckFromConfig('Deck')
    playerDeckShuffle()

    systems {
        gainResource(res: MANA_MAX, value: 10, untilMax: 100)
        restoreResources(resource: MANA, value: MANA_MAX)
        upkeepCost(filter: ownedBattlefieldCreatures, decreaseBy: MANA_COST, decrease: MANA)

        // Play
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
