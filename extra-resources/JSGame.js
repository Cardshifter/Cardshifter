"use strict";
load('keywords.js');

var PLAY_ACTION = "Play";
var ENCHANT_ACTION = "Enchant";
var ATTACK_ACTION = "Attack";
var END_TURN_ACTION = "End Turn";
var USE_ACTION = "Use";

var ATTACK = createResource("ATTACK");
var HEALTH = createResource("HEALTH");
var MAX_HEALTH = createResource("MAX_HEALTH");

var ATTACK_AVAILABLE = createResource("ATTACK_AVAILABLE");
var DENY_COUNTERATTACK = createResource("DENY_COUNTERATTACK");
var MANA = createResource("MANA");
var MANA_COST = createResource("MANA_COST");
var MANA_MAX = createResource("MANA_MAX");
var SICKNESS = createResource("SICKNESS");
var TAUNT = createResource("TAUNT");

load('keywords-creatures.js');
load('keywords-enchantments.js');
load('keywords-systems.js');
load('keyword-noattack.js');
load('keyword-scrap.js');

/**
 * Declare game configuration
 * @param {Object} game - Game configuration data
 */
function declareConfiguration(game) {
	var neutral = game.newEntity();
	var zone = new com.cardshifter.modapi.cards.ZoneComponent(neutral, "Cards");
	neutral.addComponent(zone);
	addCards(game, zone);

	/** Parameters related to DeckConfigFactory */
	var maxCardsPerType = 5;
	var minSize = 10;
	var maxSize = 10;

	/**
	 * Create playerComponent 0 & 1, i.e., Player1 & Player2
	 * Config a deck for each player
	 */
	for (var i = 0; i < 2; i++) {
		var entity = game.newEntity();
		var playerComponent = new com.cardshifter.modapi.base.PlayerComponent(i, "Player" + (i+1));
		entity.addComponent(playerComponent);
		var config = Java.type("net.zomis.cardshifter.ecs.config.DeckConfigFactory").create(minSize, maxSize, zone.getCards(), maxCardsPerType);
		var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
		entity.addComponent(new ConfigComponent().addConfig("Deck", config));
	}
}


/**
 * Contains the library of cards available for a Cardshifter game in JSON format.
 * Note: Please use the 'effects' wrapper only for properties that change the *behavior* of a card.
 *   Keep regular/arbitrary attributes outside of effects to keep things organized.
 *
 * @module CardData
 */
function addCards(game, zone) {
    applyCardKeywords(game, zone, {
        cards: [
            /** MECH CREATURES */
            {
                name: "Spareparts",
                flavor: "Cobbled together from whatever was lying around at the time.",
                creature: "Mech",
                manaCost: 0,
                health: 1,
                attack: 0,
                scrap: 3,
                sickness: 0,
                noAttack: true,
                onEndOfTurn: {
                    damage: { value: 1, target: "owner" }
                },
            },
            /** BIO CREATURES */
            {
                name: "Longshot",
                flavor: "Eyes and reflexes augmented for maximum deadliness.",
                creature: "Bio",
                manaCost: 3,
                health: 1,
                attack: 3,
                denyCounterAttack: 1
            },
            /** ENCHANTMENTS */
            {
                name: "Bionic Arms",
                flavor: "These arms will give strength to even the most puny individual.",
                enchantment: true,
                scrapCost: 1,
                addAttack: 2,
                addHealth: 0
            }
        ]
    });
}

function ownedBattlefieldCreatures(entity) {
    var Cards = Java.type("com.cardshifter.modapi.cards.Cards");
    return entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class)
            && Cards.isOnZone(entity, com.cardshifter.modapi.cards.BattlefieldComponent.class)
            && Cards.isOwnedByCurrentPlayer(entity)
}

function playerSetup(game) {
    var phaseController = new com.cardshifter.modapi.phase.PhaseController();
    game.newEntity().addComponent(phaseController);

    var players = com.cardshifter.modapi.players.Players.getPlayersInGame(game);
    for (var i = 0; i < 2; i++) {
        var playerIndex = i;
        var player = players.get(i);
        var playerPhase = new com.cardshifter.modapi.phase.Phase(player, "Main");
        phaseController.addPhase(playerPhase);

        var actions = new com.cardshifter.modapi.actions.ActionComponent();
        player.addComponent(actions);

        var isPhase = function (phase) {
            return function (act) {
                var check = phaseController.getCurrentPhase() == phase;
                return phaseController.getCurrentPhase() == phase;
            }
        }

        var endTurnAction = new com.cardshifter.modapi.actions.ECSAction(player, END_TURN_ACTION,
            isPhase(playerPhase), function (act) {
            phaseController.nextPhase();
        });
        actions.addAction(endTurnAction);

        com.cardshifter.modapi.resources.ECSResourceMap.createFor(player)
            .set(HEALTH, 30)
            .set(MAX_HEALTH, 30)
            .set(MANA, 0)
            .set(SCRAP, 0);

        var deck = new com.cardshifter.modapi.cards.DeckComponent(player);
        var hand = new com.cardshifter.modapi.cards.HandComponent(player);
        var battlefield = new com.cardshifter.modapi.cards.BattlefieldComponent(player);
        player.addComponents(hand, deck, battlefield);

        var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
        var config = player.getComponent(ConfigComponent.class);
        var deckConf = config.getConfig(com.cardshifter.api.config.DeckConfig.class);
        if (deckConf.total() < deckConf.getMinSize()) {
            deckConf.generateRandom();
        }
        setupDeck(deck, deckConf);
        deck.shuffle();
    }
}

function setupDeck(deck, deckConf) {
    var game = deck.owner.game;
    for each (var chosen in deckConf.chosen.entrySet()) {
        var entityId = chosen.key;
        var count = chosen.value;

        for (var i = 0; i < count; i++) {
            var existing = game.getEntity(entityId);
            var copy = existing.copy();
            deck.addOnBottom(copy);
        }
    }
}


function setupGame(game) {
    playerSetup(game);

    var LastPlayersStandingEndsGame = Java.type("net.zomis.cardshifter.ecs.usage.LastPlayersStandingEndsGame");
    var EffectActionSystem = Java.type("net.zomis.cardshifter.ecs.effects.EffectActionSystem");
    applySystems(game, [
        { gainResource: { res: MANA_MAX, value: 1, untilMax: 10 } },
        { restoreResources: { res: MANA, value: { res: MANA_MAX } } },

        // Play
        { playFromHand: PLAY_ACTION },
        { playEntersBattlefield: PLAY_ACTION },
        { useCost: { action: PLAY_ACTION, res: MANA, value: { res: MANA_COST }, whoPays: "player" } },

        // Enchant
        { playFromHand: ENCHANT_ACTION },
        new com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes("Bio"),
        new com.cardshifter.modapi.actions.enchant.EnchantPerform(ATTACK, HEALTH, MAX_HEALTH),

        // Spell
        { useCost: { action: USE_ACTION, res: MANA, value: { res: MANA_COST }, whoPays: "player" } },
        { playFromHand: USE_ACTION },
        new EffectActionSystem(USE_ACTION),
        new EffectActionSystem(ENCHANT_ACTION),
        new EffectActionSystem(PLAY_ACTION),
        { targetFilterSystem: USE_ACTION },
        { destroyAfterUse: USE_ACTION },

        // Attack
        new com.cardshifter.modapi.actions.attack.AttackOnBattlefield(),
        new com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer(TAUNT),
        new com.cardshifter.modapi.actions.attack.AttackSickness(SICKNESS),
        { useCost: { action: ATTACK_ACTION, res: ATTACK_AVAILABLE, value: 1, whoPays: "self" } },


        new com.cardshifter.modapi.resources.RestoreResourcesToSystem(ownedBattlefieldCreatures, ATTACK_AVAILABLE,
          function (entity) { return 1; }),
        new com.cardshifter.modapi.resources.RestoreResourcesToSystem(ownedBattlefieldCreatures, SICKNESS,
          function (entity) { return Math.max(0, SICKNESS.getFor(entity) - 1); }),
        new com.cardshifter.modapi.actions.attack.TrampleSystem(HEALTH),

        // Draw cards
        { startCards: 5 },
        new com.cardshifter.modapi.cards.DrawCardAtBeginningOfTurnSystem(),
        new com.cardshifter.modapi.cards.DamageConstantWhenOutOfCardsSystem(HEALTH, 1),
        new com.cardshifter.modapi.cards.LimitedHandSizeSystem(10, function (card) { card.getCardToDraw().destroy() }),

        // General setup
        new com.cardshifter.modapi.cards.MulliganSingleCards(game),
        new com.cardshifter.modapi.resources.GameOverIfNoHealth(HEALTH),
        new LastPlayersStandingEndsGame(),
        new com.cardshifter.modapi.cards.RemoveDeadEntityFromZoneSystem(),
        new com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer(),
    ]);

	var allowCounterAttackRes = DENY_COUNTERATTACK.retriever;
    var allowCounterAttack = function (attacker, defender) {
        return allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
    }

    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackDamageAccumulating(ATTACK, HEALTH, allowCounterAttack));
    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn(HEALTH, MAX_HEALTH));

    var ApplyAfterAttack = Java.type("net.zomis.cardshifter.ecs.usage.ApplyAfterAttack");
    game.addSystem(new ApplyAfterAttack(function (entity) {
        return allowCounterAttackRes.getFor(entity) > 0;
    }, function (entity) {
        SICKNESS.retriever.set(entity, 2);
    }));
}
