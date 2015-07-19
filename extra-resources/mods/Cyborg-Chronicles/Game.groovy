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
SCRAP = createResource("SCRAP")
SCRAP_COST = createResource("SCRAP_COST")
TRAMPLE = createResource("TRAMPLE")

PLAY_ACTION = "Play";
ENCHANT_ACTION = "Enchant";
ATTACK_ACTION = "Attack";
USE_ACTION = "Use";

include 'creatures'
include 'enchantment'
include 'scrap'
include 'noAttack'
include 'spells'

onCard('creature') {entity, args ->
    // give all creatures taunt by default
    entity.taunt = 1
}

config {
    println 'Game Closure!'

    neutral {
        resourceModifier()
        phases()
        zone('Cards') {
            cardset 'mechs'
            cardset 'bios'
            cardset 'enchantments'
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

rules {
    init {
        game.players.each {
            it.deck.createFromConfig('Deck')
            it.deck.shuffle()
            it.drawCards(5)
        }
        mulliganIndividual()
    }

    action('Play') {
        allowFor { // only allow if...
            ownedBy 'active' // ...card is owned by active player
            zone 'Hand' // ...card is on hand
        }

        // this action costs MANA to play
        // the value it costs is equal to mana-cost value of the card
        // card.owner indicates that the card's owner should pay this cost
        cost MANA value { card.mana_cost } on { card.owner }

        perform {
            card.moveTo 'Battlefield'
            effectAction() // perform an effect associated with the card
        }
    }

    action('Attack') {
        allowFor {
            ownedBy 'active'
            zone 'Battlefield'
        }
        requiresThat {
            card.SICKNESS == 0
        }
        targets 1 of {
            ownedBy opponent
        }

        cost ATTACK_AVAILABLE value 1 on { card }

        attack {
            battlefieldFirst TAUNT
            def allowCounterAttack = {attacker, defender -> attacker.deny_counterattack == 0 }
            accumulating(ATTACK, HEALTH, allowCounterAttack)
            trample(TRAMPLE, HEALTH)
        }

        perform {
            if (card.deny_counterattack > 0) {
                card.sickness = 2
            }
        }
    }

    turnStart {
        you.drawCard(1)
        you.mana_max = Math.min(1 + (int) you.mana_max, 10)
        you.mana = you.mana_max
        you.battlefield.forEach {
            it.attack_available = 1
            if (it.sickness > 0) {
                it.sickness--
            }
        }
    }

    turnEnd {
        you.battlefield.forEach {
            it.health = it.max_health
        }
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

        cost SCRAP value { card.scrap_cost } on { card.owner }
        cost MANA value { card.mana_cost } on { card.owner }
        perform {
            targets.forEach {
                it.attack += card.attack
                it.health += card.health
                it.max_health += card.health
            }
            effectAction()
            destroy()
        }
    }

    action('Use') {
        allowFor {
            ownedBy 'active'
            zone 'Hand'
        }
        cardTargetFilter()

        cost MANA value { card.mana_cost } on { card.owner }
        cost SCRAP value { card.scrap_cost } on { card.owner }

        perform {
            effectAction()
            destroy()
        }
    }

    always {
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
