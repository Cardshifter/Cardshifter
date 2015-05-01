var ZoneComponent = Java.type("com.cardshifter.modapi.cards.ZoneComponent");
var PlayerComponent = Java.type("com.cardshifter.modapi.base.PlayerComponent");
var DeckConfigFactory = Java.type("net.zomis.cardshifter.ecs.config.DeckConfigFactory");
var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
var PhrancisResources = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources");

/** 
 * Map that represents various card resources:
 * MAX_HEALTH, SNIPER, DOUBLE_ATTACK, TAUNT, DENY_COUNTERATTACK, HEALTH, MANA, MANA_MAX, SCRAP, 
 * ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE
 */
var ECSResourceMap = Java.type("com.cardshifter.modapi.resources.ECSResourceMap");
/** 
 * Map that represents various card attributes:
 * NAME, FLAVOR
 */
var ECSAttributeMap = Java.type("com.cardshifter.modapi.attributes.ECSAttributeMap");
var baseMod = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
var Attributes = Java.type("com.cardshifter.modapi.attributes.Attributes");

/**
 * EFFECT ELEMENTS LIST
 */

var effectElements = {
    triggers: {
        onEndTurn: function() {},
        onStartTurn: function() {},
        onStartGame: function() {},

        onSelfPlayerDamage: function() {},
        onOpponentPlayerDamage: function() {},
        onAnyPlayerDamage: function() {},

        onSelfCardDamage: function() {},
        onSelfCardDeath: function() {},
        onOpponentCardDamage: function() {},
        onOpponentCardDeath: function() {},
        onSelfSpecificCardDeath: function() {},
        onOpponentSpecificCardDeath: function() {}
    },
    targets: {
        selfPlayer: function() {},
        opponentPlayer: function() {},
        allPlayers: function() {},

        selfCards: function() {},
        opponentCards: function() {},
        allCards: function() {},

        selfDeck: function() {},
        opponentDeck: function() {},
        allDecks: function() {},

        selfBoard: function() {},
        opponentBoard: function() {},
        allBoards: function() {}
    },
    actions: {
        damage: function() {},
        heal: function() {},
        drawCard: function() {},
        burnCard: function() {},

        addAttack: function() {},
        addHealth: function() {},

        giveRush: function() {},
        giveRanged: function() {},
        giveTaunt: function() {},
        giveWait: function(turns, target) {
            /** EXAMPLE CODE - THIS WOULD NOT ACTUALLY WORK - ECS MAP CALL IS WRONG */
            1 += turns;
            ECSResourceMap.createFor(target).set(Resources.SICKNESS, turns);
        }
    },
    others: {
        chance: function(probability) {
            if (probability > 1) { probability = 1; }
            if (probability < 0) { probability = 0; }
            if ((typeof probability) !== "number" && isNaN(probability)) {
                return NaN;
            } else {
                var roll = Math.random();
                return (roll <= probability);
            }
        }
    }
};
