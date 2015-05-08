"use strict";

/**
 * Sets card name atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {string} - The card's NAME. 
 */
keywords.cards.name = function (entity, obj, value) {
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.Attributes.NAME, value);
}
/**
 * Sets card flavor text atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {string} - The card's FLAVOR text. 
 */
keywords.cards.flavor = function (entity, obj, value) {
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.Attributes.FLAVOR, value);
}
/**
 * Sets card as a creature, and related Actions.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {string} - The card's creature type. 
 */
keywords.cards.creature = function (entity, obj, value) {
    var actions = new com.cardshifter.modapi.actions.ActionComponent();
	entity.addComponent(actions);

    var playAction = new ECSAction(entity, PLAY_ACTION, function (act) { return true; }, function (act) {});
    var attackAction = new ECSAction(entity, ATTACK_ACTION, function (act) { return true; }, function (act) {}).addTargetSet(1, 1);

	actions.addAction(playAction);
	actions.addAction(attackAction);

	entity.addComponent(new com.cardshifter.modapi.base.CreatureTypeComponent(value));

	var map = com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity);
	map.set(SICKNESS, 1);
	map.set(TAUNT, 1);
	map.set(ATTACK_AVAILABLE, 1);

}

/**
 * Sets card mana cost atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {int} - The card's MANA_COST. 
 */
keywords.cards.manaCost = function (entity, obj, value) {
	MANA_COST.retriever.set(entity, value);
}

/**
 * Checks whether the card is not a creature, otherwise sets card health atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {int} - The card's HEALTH. 
 */
keywords.cards.health = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	HEALTH.retriever.set(entity, value);
	MAX_HEALTH.retriever.set(entity, value);
}

/**
 * Checks whether the card is not a creature, otherwise sets card attack atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {int} - The card's ATTACK. 
 */
keywords.cards.attack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	ATTACK.retriever.set(entity, value);
}
/**
 * Checks whether the card is not a creature, otherwise sets card sickness atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {int} - The card's SICKNESS. 
 */
keywords.cards.sickness = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	SICKNESS.retriever.set(entity, value);
}

/**
 * Checks whether the card is not a creature, otherwise sets card denyCounterAttack atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {int} - The card's DENY_COUNTERATTACK. 
 */
keywords.cards.denyCounterAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	DENY_COUNTERATTACK.retriever.set(entity, value);
}

/**
 * Checks whether it has conflicting triggers to whilePresent, 
 *  otherwise sets card onEndOfTurn atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {Object} - The card's onEndOfTurn properties. 
 */
keywords.cards.whilePresent = function (entity, obj, value) {
    if (obj.afterPlay || obj.onEndOfTurn) {
        throw new Error("whilePresent cannot exist together with afterPlay or onEndOfTurn at the moment");
    }

    var eff = Java.type("net.zomis.cardshifter.ecs.effects.Effects");
    eff = new eff();
    var description = resolveModifierDescriptions(value);
    entity.addComponent(
        eff.described(description,
            eff.toSelf(
                function (me) {
                    var resMod = com.cardshifter.modapi.resources.ResourceModifierComponent.class;
                    var resModifierObject = com.cardshifter.modapi.base.ComponentRetriever.singleton(entity.game, resMod);

                    var modifiers = resolveModifiers(me, value);
                    for each (var modifier in modifiers) {
                        resModifierObject.addModifier(modifier.res, modifier.object);
                    }
                }
            )
        )
    );
};

/**
 * Checks whether the card is a creature, 
 *  whether it has conflicting triggers to onEndOfTurn, 
 *  otherwise sets card onEndOfTurn atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {Object} - The card's onEndOfTurn properties. 
 */
keywords.cards.onEndOfTurn = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
    if (obj.afterPlay) {
        throw new Error("afterPlay and onEndOfTurn can't co-exist at the moment");
    }
    var eff = Java.type("net.zomis.cardshifter.ecs.effects.Effects");
    eff = new eff();
    var effect = applyEffect(value);
    entity.addComponent(
        eff.described(effect.description + " at end of turn",
            eff.giveSelf(
                eff.triggerSystem(com.cardshifter.modapi.phase.PhaseEndEvent.class,
                    function (me, event) {
                        return com.cardshifter.modapi.players.Players.findOwnerFor(me) == event.getOldPhase().getOwner();
                    },
                    function (source, event) {
                        effect.action(source, source);
                    }
                )
            )
        )
    );
}

/**
 * Checks whether it has conflicting triggers to afterPlay, 
 *  otherwise sets card afterPlay atrributes.
 * @param entity {Object} - The card entity.
 * @param obj {Object} - The applicable card object.
 * @param value {Object} - The card's afterPlay properties. 
 */
keywords.cards.afterPlay = function (entity, obj, value) {
    if (obj.onEndOfTurn) {
        throw new Error("afterPlay and onEndOfTurn can't co-exist at the moment");
    }
    var eff = Java.type("net.zomis.cardshifter.ecs.effects.Effects");
    eff = new eff();
    var effect = applyEffect(value);
    entity.addComponent(
        eff.described(effect.description,
            eff.toSelf(
                function (source) {
                    effect.action(source, null);
                }
            )
        )
    );
}
