def ownedBattlefieldCreatures = {entity ->
    def Cards = com.cardshifter.modapi.cards.Cards;
    return entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class) &&
            Cards.isOnZone(entity, com.cardshifter.modapi.cards.BattlefieldComponent.class) &&
            Cards.isOwnedByCurrentPlayer(entity)
}

ATTACK = createResource('ATTACK')
HEALTH = createResource("HEALTH")
MAX_HEALTH = createResource("MAX_HEALTH")

ATTACK_AVAILABLE = createResource("ATTACK_AVAILABLE")
DENY_COUNTERATTACK = createResource("DENY_COUNTERATTACK")
MANA = createResource("MANA")
MANA_COST = createResource("MANA_COST")
MANA_MAX = createResource("MANA_MAX")
SICKNESS = createResource("SICKNESS")
TAUNT = createResource("TAUNT")

PLAY_ACTION = "Play";
ATTACK_ACTION = "Attack";
USE_ACTION = "Use";

resources([ATTACK, HEALTH, MAX_HEALTH, ATTACK_AVAILABLE, DENY_COUNTERATTACK, MANA, MANA_COST, MANA_MAX,
           SICKNESS, TAUNT])

include 'creatures'
include 'noAttack'

config {
    neutral {
        resourceModifier()
        phases()
        zone 'Cards', {
            cardset 'common'
            cardset 'chinese'
            //cardset 'egyptian'
            //cardset 'greek'
            //cardset 'norse'
            //cardset 'hindu'
            //cardset 'roman'
        }
    }

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
        mana 100
        mana_max 100
    }
}

setup {
    playerDeckFromConfig('Deck')
    playerDeckShuffle()

    def removeDead = {
        game.events.registerHandlerAfter(this, com.cardshifter.modapi.actions.ActionPerformEvent.class, {event ->
            def battlefield = com.cardshifter.modapi.cards.BattlefieldComponent.class;
            def remove = event.entity.game.getEntitiesWithComponent(battlefield)
                    .stream().flatMap({entity -> entity.getComponent(battlefield).stream()})
                    .peek({e -> println("$e has ${HEALTH.getFor(e)}")})
                    .filter({e -> HEALTH.getFor(e) <= 0})
                    .collect(java.util.stream.Collectors.toList());
            for (def e in remove) {
                e.destroy();
            }
        })
    }

    systems {
//        gainResource(res: MANA_MAX, value: 1, untilMax: 10)
        restoreResources(resource: MANA, value: MANA_MAX)
        upkeepCost(filter: ownedBattlefieldCreatures, decreaseBy: MANA_COST, decrease: MANA)

        // Play
        playFromHand PLAY_ACTION
        playEntersBattlefield PLAY_ACTION
        useCost(action: PLAY_ACTION, res: MANA, value: MANA_COST, whoPays: "player")

        // Spell
        useCost(action: USE_ACTION, res: MANA, value: MANA_COST, whoPays: "player")
        playFromHand USE_ACTION
        EffectActionSystem(USE_ACTION)
        EffectActionSystem(PLAY_ACTION)
        targetFilterSystem USE_ACTION
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
        removeDead()
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
            healAtEndOfTurn(HEALTH, MAX_HEALTH)
            afterAttack({entity -> allowCounterAttackRes.getFor(entity) > 0},
                    { entity -> SICKNESS.retriever.set(entity, 2) })
            trample HEALTH
        }
    }
}
