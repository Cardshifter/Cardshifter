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
load('keywords-effects.js');
load('keyword-noattack.js');
load('keyword-scrap.js');

/**
 * Declare game configuration
 * @param {Object} game - Game configuration data
 */
function declareConfiguration(game) {
	var neutral = game.newEntity();
    neutral.addComponent(new com.cardshifter.modapi.resources.ResourceModifierComponent());
	var zone = new com.cardshifter.modapi.cards.ZoneComponent(neutral, "Cards");

	neutral.addComponent(zone);
	addCards(game, zone);

	/** Parameters related to DeckConfigFactory */
	var maxCardsPerType = 3;
	var minSize = 30;
	var maxSize = 30;

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
 * See the game documentation for an explanation of the usage of each 
 *  attribute and effect.
 * @param {Object} game - An instance of the game.
 * @param {Object} zone - Zone to where the cards should be added.
 */
function addCards(game, zone) {
    applyCardKeywords(game, zone, {
        cards: [
            //Mech creatures
            {
                name: "Spareparts",
                creature: "Mech",
                health: 1,
                attack: 0,
                sickness: 0,
                scrap: 3,
                manaCost: 0,
                flavor: "Cobbled together from whatever was lying around at the time."
            },
            {
                name: "Gyrodroid",
                creature: "Mech",
                health: 1,
                denyCounterAttack: 1,
                attack: 1,
                manaCost: 1,
                scrap: 1,
                flavor: "A flying, spherical droid that shoots weak laser beams at nearby targets."
            },
            {
                name: "The Chopper",
                creature: "Mech",
                health: 1,
                sickness: 0,
                manaCost: 2,
                attack: 2,
                scrap: 1,
                flavor: "Looks like a flying circular blade with a sphere in the middle."
            },
            {
                name: "Shieldmech",
                creature: "Mech",
                health: 3,
                manaCost: 2,
                attack: 1,
                scrap: 1,
                flavor: "A small, flying shield generator droid."
            },
            {
                name: "Robot Guard",
                creature: "Mech",
                health: 2,
                manaCost: 2,
                attack: 2,
                scrap: 1,
                flavor: "Common and inexpensive robot often use for personal protection."
            },
            {
                name: "Humadroid",
                creature: "Mech",
                health: 3,
                manaCost: 3,
                attack: 3,
                scrap: 2,
                flavor: "You might mistake it for a human, but it won’t mistake you for a mech."
            },
            {
                name: "Assassinatrix",
                creature: "Mech",
                health: 1,
                denyCounterAttack: 1,
                manaCost: 3,
                attack: 4,
                scrap: 2,
                flavor: "Humanoid in form, except for two massive cannons in place of arms."
            },
            {
                name: "Fortimech",
                creature: "Mech",
                health: 4,
                manaCost: 3,
                attack: 2,
                scrap: 2,
                flavor: "About the only place that a person is safe during a firefight is inside one of these."
            },
            {
                name: "Scout Mech",
                creature: "Mech",
                health: 1,
                sickness: 0,
                manaCost: 3,
                attack: 5,
                scrap: 2,
                flavor: "The fastest mech on two legs. You don’t want to see the ones with four."
            },
            {
                name: "Supply Mech",
                creature: "Mech",
                health: 5,
                attack: 0,
                sickness: 0,
                manaCost: 3,
                scrap: 3,
                flavor: "Worth more than its weight in scrap, and it is pretty heavy.",
            },
            {
                name: "F.M.U.",
                creature: "Mech",
                health: 4,
                attack: 0,
                onEndOfTurn: { heal: { value: 1, target: "owner" } },
                manaCost: 4,
                scrap: 2,
                flavor: "The Field Medical Unit is equipped with modern laser surgical tools and a variety of remedy shots."
            },
            {
                name: "Modleg Ambusher",
                creature: "Mech",
                health: 3,
                sickness: 0,
                manaCost: 6,
                attack: 5,
                scrap: 3,
                flavor: "Uses the legs of other bots to enhance its own speed."
            },
            {
                name: "Heavy Mech",
                creature: "Mech",
                health: 6,
                manaCost: 5,
                attack: 3,
                scrap: 3,
                flavor: "The bigger they are, the harder they fall. Eventually."
            },
            {
                name: "Waste Runner",
                creature: "Mech",
                manaCost: 5,
                attack: 4,
                health: 4,
                scrap: 3,
                flavor: "Armored and armed with superior arms.",
            },
            // Bio creatures
            {
                name: "Conscript",
                creature: "Bio",
                health: 2,
                sickness: 0,
                manaCost: 2,
                attack: 2,
                flavor: "He just signed up last week and he is very excited to fight."
            },
            {
                name: "Longshot",
                creature: "Bio",
                health: 1,
                denyCounterAttack: 1,
                manaCost: 3,
                attack: 3,
                flavor: "Eyes and reflexes augmented for maximum deadliness."
            },
            {
                name: "Bodyman",
                creature: "Bio",
                manaCost: 4,
                attack: 2,
                health: 3,
                flavor: "Strength augmented with mechanical musculature."
            },
            {
                name: "Vetter",
                creature: "Bio",
                health: 3,
                manaCost: 5,
                attack: 3,
                flavor: "A retired conscript with a desire to jack and make some quick creds."
            },
            {
                name: "Field Medic",
                creature: "Bio",
                health: 5,
                onEndOfTurn: { 
                    heal: { 
                        value: 1, 
                        target: "owner" 
                    } 
                },
                manaCost: 5,
                attack: 1,
                flavor: "Unsung hero responsible for keeping countless troops alive."
            },
            {
                name: "Wastelander",
                creature: "Bio",
                health: 4,
                manaCost: 6,
                attack: 4,
                flavor: "Spent his life learning the lessons of the wastelands."
            },
            {
                name: "Commander",
                creature: "Bio",
                health: 3,
                sickness: 0,
                manaCost: 6,
                attack: 5,
                flavor: "A professional soldier for the government."
            },
            {
                name: "Cyberpimp",
                creature: "Bio",
                health: 5,
                manaCost: 6,
                attack: 3,
                flavor: "Supersized and heavily augmented."
            },
            {
                name: "Cyborg",
                creature: "Bio",
                health: 5,
                manaCost: 7,
                attack: 5,
                flavor: "He’s more machine than human now."
            },
            {
                name: "Web Boss",
                creature: "Bio",
                health: 6,
                manaCost: 8,
                attack: 6,
                flavor: "Leader of a gang that primarily operates on the web."
            },
            {
                name: "Inside Man",
                creature: "Bio",
                health: 6,
                attack: 2,
                afterPlay: {
                    summon: {
                        count: 1,
                        card: "Bodyman",
                        where: "Battlefield",
                        who: "owner"
                    }
                },
                manaCost: 8,
                flavor: "A government official with wider web control. Usually brings friends."
            },
            // Enchantments
            {
                name: "Bionic Arms",
                enchantment: true,
                addAttack: 2,
                scrapCost: 1,
                flavor: "These arms will give strength to even the most puny individual."
            },
            {
                name: "Body Armor",
                enchantment: true,
                addHealth: 2,
                scrapCost: 1,
                flavor: "Steel-reinforced armor to absord damage from blows and shots."
            },
            {
                name: "Adrenalin Injection",
                enchantment: true,
                set: { res: SICKNESS, value: 0 },
                addAttack: 1,
                addHealth: 1,
                scrapCost: 1,
                flavor: "An injection to increase speed and body function."
            },
            {
                name: "Steroid Implants",
                enchantment: true,
                addAttack: 2,
                addHealth: 1,
                scrapCost: 2,
                flavor: "Intraveneous implants that feed the body for increased strength."
            },
            {
                name: "Reinforced Cranial Implants",
                enchantment: true,
                addAttack: 1,
                addHealth: 2,
                scrapCost: 2,
                flavor: "Offers head protection as well as a slight increase in brain activity."
            },
            {
                name: "Cybernetic Arm Cannon",
                enchantment: true,
                set: { res: DENY_COUNTERATTACK, value: 1 },
                addAttack: 3,
                addHealth: 0,
                scrapCost: 2,
                flavor: "Replaces the forearm with a powerful firearm for massive damage."
            },
            {
                name: "Exoskeleton",
                enchantment: true,
                addHealth: 3,
                scrapCost: 2,
                flavor: "This very invasive operation reinforces bone tissue with titanium."
            },
            {
                name: "Artificial Intelligence Implants",
                enchantment: true,
                addAttack: 2,
                addHealth: 3,
                scrapCost: 3,
                flavor: "An advanced processor is connected to the subject's brain, replacing personality with extreme intelligence and reflexes."
            },
            {
                name: "Full-body Cybernetics Upgrade",
                enchantment: true,
                addAttack: 3,
                addHealth: 3,
                scrapCost: 5,
                flavor: "Most of the subject's body is converted to cybernetics, increasing strength and resilience substantially."
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
        new EffectActionSystem(ENCHANT_ACTION), // needs to be before EnchantPerform, because of entity removal
        new com.cardshifter.modapi.actions.enchant.EnchantPerform(ATTACK, HEALTH, MAX_HEALTH),

        // Spell
        { useCost: { action: USE_ACTION, res: MANA, value: { res: MANA_COST }, whoPays: "player" } },
        { playFromHand: USE_ACTION },
        new EffectActionSystem(USE_ACTION),
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
