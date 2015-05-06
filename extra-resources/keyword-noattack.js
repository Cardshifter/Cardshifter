"use strict";
var noAttackCreatures = new java.util.HashSet();

/**
 * Checks if a card has a valid noAttack property and is a creature.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {bool} - Whether the card has noAttack.
 */
keywords.cards.noAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	noAttackCreatures.add(obj.name);
}

/**
 * Adds the noAttack property to applicable cards.
 * @param game {Object} - The game entity.
 * @param cardData {Object} - The applicable card data.
 * @param cardEntities {Object} - The applicable cardEntities.
 */
keywords.afterCards.push(function (game, cardData, cardEntities) {
    var System = Java.type("net.zomis.cardshifter.ecs.usage.DenyActionForNames");
	game.addSystem(new System(ATTACK_ACTION, noAttackCreatures));
});
