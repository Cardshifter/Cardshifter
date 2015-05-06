"use strict";

/**
 * Checks if a card is a valid Enchantment, and adds the enchantment property.
 * @param {Object} entity - The applicable card entity.
 * @param {Object} obj - The applicable card object.
 * @param {boolean} value - Whether the card is an Enchantment.
 */
keywords.cards.enchantment = function (entity, obj, value) {
    if (obj.creature) {
        throw new Error("cannot be both enchantment and creature at once");
    }
    var actions = new com.cardshifter.modapi.actions.ActionComponent();
    var enchantAction = new ECSAction(entity, ENCHANT_ACTION, function (act) { return true; }, function (act) {}).addTargetSet(1, 1);
    actions.addAction(enchantAction);
	entity.addComponent(actions);
}

/**
 * Checks for and enables addAttack property.
 * @param {Object} entity - The applicable card entity.
 * @param {Object} obj - The applicable card object.
 * @param {int} value - The attack value to be added to the target.
 */
keywords.cards.addAttack = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    ATTACK.retriever.set(entity, value);
}

/**
 * Checks for and enables addHealth property.
 * @param {Object} entity - The applicable card entity.
 * @param {Object} obj - The applicable card object.
 * @param {int} value - The health value to be added to the target.
 */
keywords.cards.addHealth = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    HEALTH.retriever.set(entity, value);
    MAX_HEALTH.retriever.set(entity, value);
}

/**
 * Applies Enchantment to target.
 * @param {Object} entity - The applicable card entity.
 * @param {Object} obj - The applicable card object.
 * @param {Object} value - The value to be added to the target.
 */
keywords.cards.set = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    var eff = Java.type("net.zomis.cardshifter.ecs.effects.Effects");
    eff = new eff();

    entity.addComponent(
        eff.described("Set " + value.res + " to " + valueDescription(value.value),
            eff.giveTarget(value.res, 1, function(i) {
                var val = valueLookup(value.value);
                return val;
            })
        )
    );

}

