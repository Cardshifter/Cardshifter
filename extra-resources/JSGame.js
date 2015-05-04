load('keywords.js');
load('keywords-creatures.js');
load('keywords-enchantments.js');
load('keywords-systems.js');

var PLAY_ACTION = "Play";
var ENCHANT_ACTION = "Enchant";
var ATTACK_ACTION = "Attack";
var SCRAP_ACTION = "Scrap";
var END_TURN_ACTION = "End Turn";
var USE_ACTION = "Use";

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
    print("addCards called");


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
                noAttack: true
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

        function isPhase(phase) {
            return function (act) {
                var check = phaseController.getCurrentPhase() == phase;
                print("action allowance check: " + act + " phase " + phaseController + " compare " + phase +
                 " current " + phaseController.getCurrentPhase() + " equals " + check);
                return phaseController.getCurrentPhase() == phase;
            }
        }

        var endTurnAction = new com.cardshifter.modapi.actions.ECSAction(player, END_TURN_ACTION,
            isPhase(playerPhase), function (act) {
            phaseController.nextPhase();
        });
        print("action: " + endTurnAction);
        actions.addAction(endTurnAction);

        com.cardshifter.modapi.resources.ECSResourceMap.createFor(player)
            .set(pgres.HEALTH, 30)
            .set(pgres.MAX_HEALTH, 30)
            .set(pgres.MANA, 0)
            .set(pgres.SCRAP, 0);

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
    var ScrapSystem = Java.type("net.zomis.cardshifter.ecs.usage.ScrapSystem");
    applySystems(game, [
        { gainResource: { res: pgres.MANA_MAX, value: 1, untilMax: 10 } },
        { restoreResources: { res: pgres.MANA, value: { res: pgres.MANA_MAX } } },

        // Play
        { playFromHand: PLAY_ACTION },
        { playEntersBattlefield: PLAY_ACTION },
        { useCost: { action: PLAY_ACTION, res: pgres.MANA, value: { res: pgres.MANA_COST }, whoPays: "player" } },

        // Scrap
        new ScrapSystem(pgres.SCRAP, function (entity) {
            return com.cardshifter.modapi.resources.Resources.getOrDefault(entity, pgres.ATTACK_AVAILABLE, 0) > 0
             && com.cardshifter.modapi.resources.Resources.getOrDefault(entity, pgres.SICKNESS, 1) == 0;
        }),

        // Enchant
        { playFromHand: ENCHANT_ACTION },
        { useCost: { action: ENCHANT_ACTION, res: pgres.SCRAP, value: { res: pgres.SCRAP_COST }, whoPays: "player" } },
        new com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes("Bio"),
        new com.cardshifter.modapi.actions.enchant.EnchantPerform(pgres.ATTACK, pgres.HEALTH, pgres.MAX_HEALTH),

        // Spell
        { useCost: { action: USE_ACTION, res: pgres.MANA, value: { res: pgres.MANA_COST }, whoPays: "player" } },
        { useCost: { action: USE_ACTION, res: pgres.SCRAP, value: { res: pgres.SCRAP_COST }, whoPays: "player" } },
        { playFromHand: USE_ACTION },
        new EffectActionSystem(USE_ACTION),
        new EffectActionSystem(ENCHANT_ACTION),
        new EffectActionSystem(PLAY_ACTION),
        { targetFilterSystem: USE_ACTION },
        { destroyAfterUse: USE_ACTION },

        // Attack
        new com.cardshifter.modapi.actions.attack.AttackOnBattlefield(),
        new com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer(pgres.TAUNT),
        new com.cardshifter.modapi.actions.attack.AttackSickness(pgres.SICKNESS),
        { useCost: { action: ATTACK_ACTION, res: pgres.ATTACK_AVAILABLE, value: 1, whoPays: "self" } },


        new com.cardshifter.modapi.resources.RestoreResourcesToSystem(ownedBattlefieldCreatures, pgres.ATTACK_AVAILABLE,
          function (entity) { return 1; }),
        new com.cardshifter.modapi.resources.RestoreResourcesToSystem(ownedBattlefieldCreatures, pgres.SICKNESS,
          function (entity) { return Math.max(0, pgres.SICKNESS.getFor(entity) - 1); }),
        new com.cardshifter.modapi.actions.attack.TrampleSystem(pgres.HEALTH),

        // Draw cards
        { startCards: 5 },
        new com.cardshifter.modapi.cards.DrawCardAtBeginningOfTurnSystem(),
        new com.cardshifter.modapi.cards.DamageConstantWhenOutOfCardsSystem(pgres.HEALTH, 1),
        new com.cardshifter.modapi.cards.LimitedHandSizeSystem(10, function (card) { card.getCardToDraw().destroy() }),

        // General setup
        new com.cardshifter.modapi.cards.MulliganSingleCards(game),
        new com.cardshifter.modapi.resources.GameOverIfNoHealth(pgres.HEALTH),
        new LastPlayersStandingEndsGame(),
        new com.cardshifter.modapi.cards.RemoveDeadEntityFromZoneSystem(),
        new com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer(),
    ]);

	var allowCounterAttackRes = com.cardshifter.modapi.resources.ResourceRetriever.forResource(pgres.DENY_COUNTERATTACK);
    var allowCounterAttack = function (attacker, defender) {
        return allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
    }

    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackDamageAccumulating(pgres.ATTACK, pgres.HEALTH, allowCounterAttack));
    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn(pgres.HEALTH, pgres.MAX_HEALTH));

    var ApplyAfterAttack = Java.type("net.zomis.cardshifter.ecs.usage.ApplyAfterAttack");
    game.addSystem(new ApplyAfterAttack(function (entity) {
        return allowCounterAttackRes.getFor(entity) > 0;
    }, function (entity) {
        var sickness = com.cardshifter.modapi.resources.ResourceRetriever.forResource(pgres.SICKNESS);
        sickness.resFor(entity).set(2)
    }));
}
