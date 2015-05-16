import GroovyMod;

class MyGame extends GroovyMod {

    def config() {
        game {
            println 'Game Closure!'
            neutral {
                resourceModifier()
                zone 'Cards', {
                    addCards()
                }
            }

            players(2) {
                config {
                    deck {
                        minSize 30
                        maxSize 30
                        maxCardsPerType 3
                        zone 'Cards'
                    }
                }
            }
        }
    }

    def setup() {
        println 'setup game called ' + game
    }

}

new MyGame()

/*
def PLAY_ACTION = "Play";
def ENCHANT_ACTION = "Enchant";
def ATTACK_ACTION = "Attack";
def END_TURN_ACTION = "End Turn";
def USE_ACTION = "Use";

def ATTACK = createResource("ATTACK");
def HEALTH = createResource("HEALTH");
def MAX_HEALTH = createResource("MAX_HEALTH");

def ATTACK_AVAILABLE = createResource("ATTACK_AVAILABLE");
def DENY_COUNTERATTACK = createResource("DENY_COUNTERATTACK");
def MANA = createResource("MANA");
def MANA_COST = createResource("MANA_COST");
def MANA_MAX = createResource("MANA_MAX");
def SICKNESS = createResource("SICKNESS");
def TAUNT = createResource("TAUNT");

def ownedBattlefieldCreatures = {entity ->
    def Cards = Java.type("com.cardshifter.modapi.cards.Cards");
    return entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class) &&
        Cards.isOnZone(entity, com.cardshifter.modapi.cards.BattlefieldComponent.class) &&
        Cards.isOwnedByCurrentPlayer(entity)
}

def playerSetup(game) {
    def phaseController = new com.cardshifter.modapi.phase.PhaseController();
    game.newEntity().addComponent(phaseController);

    def players = com.cardshifter.modapi.players.Players.getPlayersInGame(game);
    for (int i = 0; i < 2; i++) {
        int playerIndex = i;
        def player = players.get(i);
        def playerPhase = new com.cardshifter.modapi.phase.Phase(player, "Main");
        phaseController.addPhase(playerPhase);

        def actions = new com.cardshifter.modapi.actions.ActionComponent();
        player.addComponent(actions);

        def isPhase = function (phase) {
            return function (act) {
                var check = phaseController.getCurrentPhase() == phase;
                return phaseController.getCurrentPhase() == phase;
            }
        }

        def endTurnAction = new com.cardshifter.modapi.actions.ECSAction(player, END_TURN_ACTION,
                isPhase(playerPhase), { act -> phaseController.nextPhase(); })
        actions.addAction(endTurnAction);

        com.cardshifter.modapi.resources.ECSResourceMap.createFor(player)
                .set(HEALTH, 30)
                .set(MAX_HEALTH, 30)
                .set(MANA, 0)
                .set(SCRAP, 0);

        def deck = new com.cardshifter.modapi.cards.DeckComponent(player);
        def hand = new com.cardshifter.modapi.cards.HandComponent(player);
        def battlefield = new com.cardshifter.modapi.cards.BattlefieldComponent(player);
        player.addComponents(hand, deck, battlefield);

        def ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
        def config = player.getComponent(ConfigComponent.class);
        def deckConf = config.getConfig(com.cardshifter.api.config.DeckConfig.class);
        if (deckConf.total() < deckConf.getMinSize()) {
            deckConf.generateRandom();
        }
        setupDeck(deck, deckConf);
        deck.shuffle();
    }
}

def setupDeck(deck, deckConf) {
    def game = deck.owner.game;
    for (def chosen in deckConf.chosen.entrySet()) {
        def entityId = chosen.key;
        def count = chosen.value;

        for (int i = 0; i < count; i++) {
            var existing = game.getEntity(entityId);
            var copy = existing.copy();
            deck.addOnBottom(copy);
        }
    }
}


def setupGame2(game) {
    playerSetup(game)

    var LastPlayersStandingEndsGame = Java.type("net.zomis.cardshifter.ecs.usage.LastPlayersStandingEndsGame");
    var EffectActionSystem = Java.type("net.zomis.cardshifter.ecs.effects.EffectActionSystem");

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
            }
        )
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
        AttackSickness SICKNESS
        useCost(action: ATTACK_ACTION, res: ATTACK_AVAILABLE, value: 1, whoPays: "self")


        RestoreResourcesToSystem(ownedBattlefieldCreatures, ATTACK_AVAILABLE, {ent -> 1})
        RestoreResourcesToSystem(ownedBattlefieldCreatures, SICKNESS, {ent -> Math.max(0, SICKNESS.getFor(ent) - 1)})
        TrampleSystem HEALTH

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
    }

    def allowCounterAttackRes = DENY_COUNTERATTACK.retriever;
    def allowCounterAttack = function (attacker, defender) {
        return allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
    }

    game.addSystem(AttackDamageAccumulating(ATTACK, HEALTH, allowCounterAttack))
    game.addSystem(AttackDamageHealAtEndOfTurn(HEALTH, MAX_HEALTH))

    game.addSystem(ApplyAfterAttack({entity -> allowCounterAttackRes.getFor(entity) > 0}),
        { entity -> SICKNESS.retriever.set(entity, 2) })
}
*/