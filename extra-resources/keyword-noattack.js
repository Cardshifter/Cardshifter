"use strict";
var noAttackCreatures = new java.util.HashSet();

/**
 * Checks if a card has a valid noAttack property and is a creature.
 * @param {Object} entity - The applicable card entity.
 * @param {Object} obj - The applicable card object.
 * @param {boolean} value - Whether the card has noAttack.
 */
keywords.cards.noAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	noAttackCreatures.add(obj.name);
}

/**
 * Adds the noAttack property to applicable cards.
 * @param {Object} game - The game entity.
 * @param {Object} cardData - The applicable card data.
 * @param {boolean} cardEntities - The applicable cardEntities.
 */
keywords.afterCards.push(function (game, cardData, cardEntities) {
    var System = Java.type("net.zomis.cardshifter.ecs.usage.DenyActionForNames");
	game.addSystem(new System(ATTACK_ACTION, noAttackCreatures));
});
