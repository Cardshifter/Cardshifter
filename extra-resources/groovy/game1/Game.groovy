def ownedBattlefieldCreatures = {entity ->
    def Cards = com.cardshifter.modapi.cards.Cards;
    return entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class) &&
            Cards.isOnZone(entity, com.cardshifter.modapi.cards.BattlefieldComponent.class) &&
            Cards.isOwnedByCurrentPlayer(entity)
}

def ATTACK = createResource('ATTACK')
def HEALTH = createResource("HEALTH")
def MAX_HEALTH = createResource("MAX_HEALTH")

def ATTACK_AVAILABLE = createResource("ATTACK_AVAILABLE")
def DENY_COUNTERATTACK = createResource("DENY_COUNTERATTACK")
def MANA = createResource("MANA")
def MANA_COST = createResource("MANA_COST")
def MANA_MAX = createResource("MANA_MAX")
def SICKNESS = createResource("SICKNESS")
def TAUNT = createResource("TAUNT")
def SCRAP = createResource("SCRAP")

def PLAY_ACTION = "Play";
def ENCHANT_ACTION = "Enchant";
def ATTACK_ACTION = "Attack";
def END_TURN_ACTION = "End Turn";
def USE_ACTION = "Use";

def a = 1

def bc = {String some ->
    println 'some lambda: ' + some
    "Result is $some"
}

println a
println bc
println bc('test')

config {
    println 'value of a is ' + a
    resources([ATTACK, HEALTH, MAX_HEALTH, ATTACK_AVAILABLE, DENY_COUNTERATTACK, MANA, MANA_COST, MANA_MAX,
               SICKNESS, TAUNT, SCRAP])
    println 'Game Closure!'

    neutral {
        resourceModifier()
        phases()
        zone 'Cards', {Closure cl ->
            new Cards().addCards(cl)
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
        mana 0
        scrap 0
    }
}

setup {
    println 'setup game called ' + game
    test()
    playerDeckFromConfig('Deck')
    playerDeckShuffle()

    def removeDead = {
        game.events.registerHandlerAfter(this, com.cardshifter.modapi.actions.ActionPerformEvent.class, {event ->
            def battlefield = com.cardshifter.modapi.cards.BattlefieldComponent.class;
            def remove = event.entity.game.getEntitiesWithComponent(battlefield)
                    .stream().flatMap({entity -> entity.getComponent(battlefield).stream()})
                    .peek({e -> print(e + " has " + HEALTH.getFor(e))})
                    .filter({e -> HEALTH.getFor(e) <= 0})
                    .collect(java.util.stream.Collectors.toList());
            for (def e in remove) {
                e.destroy();
            }
        })
    }

    systems {
        gainResource(res: MANA_MAX, value: 1, untilMax: 10)
        restoreResources(res: MANA, value: { res MANA_MAX })

        // Play
        playFromHand PLAY_ACTION
        playEntersBattlefield PLAY_ACTION
        useCost(action: PLAY_ACTION, res: MANA, value: { res MANA_COST }, whoPays: "player")

        // Enchant
        playFromHand ENCHANT_ACTION
        EnchantTargetCreatureTypes("Bio")
        EffectActionSystem(ENCHANT_ACTION) // needs to be before EnchantPerform, because of entity removal
        EnchantPerform(ATTACK, HEALTH, MAX_HEALTH)

        // Spell
        useCost(action: USE_ACTION, res: MANA, value: { res MANA_COST }, whoPays: "player")
        playFromHand USE_ACTION,
                EffectActionSystem(USE_ACTION)
        EffectActionSystem(PLAY_ACTION)
        targetFilterSystem USE_ACTION
        destroyAfterUse USE_ACTION

        // Attack
        AttackOnBattlefield()
        AttackTargetMinionsFirstThenPlayer(TAUNT)
        AttackSickness(SICKNESS)
        useCost(action: ATTACK_ACTION, res: ATTACK_AVAILABLE, value: 1, whoPays: "self")


        RestoreResourcesToSystem(ownedBattlefieldCreatures, ATTACK_AVAILABLE, {ent -> 1})
        RestoreResourcesToSystem(ownedBattlefieldCreatures, SICKNESS, {ent -> Math.max(0, SICKNESS.getFor(ent) - 1)})
        TrampleSystem(HEALTH)

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
            accumulating(ATTACK, HEALTH, allowCounterAttack)
            healAtEndOfTurn(HEALTH, MAX_HEALTH)
            afterAttack({entity -> allowCounterAttackRes.getFor(entity) > 0},
                    { entity -> SICKNESS.retriever.set(entity, 2) })
        }
    }
}
