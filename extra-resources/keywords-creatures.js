"use strict";
keywords.cards.name = function (entity, obj, value) {
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.Attributes.NAME, value);
}
keywords.cards.flavor = function (entity, obj, value) {
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.Attributes.FLAVOR, value);
}
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

keywords.cards.manaCost = function (entity, obj, value) {
	MANA_COST.retriever.set(entity, value);
}

keywords.cards.health = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	HEALTH.retriever.set(entity, value);
	MAX_HEALTH.retriever.set(entity, value);
}

keywords.cards.attack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	ATTACK.retriever.set(entity, value);
}


keywords.cards.sickness = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	SICKNESS.retriever.set(entity, value);
}

keywords.cards.denyCounterAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	DENY_COUNTERATTACK.retriever.set(entity, value);
}

keywords.cards.whilePresent = function (entity, obj, value) {
    if (obj.afterPlay || obj.onEndOfTurn) {
        throw new Error("whilePresent cannot exist together with afterPlay or onEndOfTurn at the moment");
    }

    var eff = Java.type("net.zomis.cardshifter.ecs.effects.Effects");
    eff = new eff();
    entity.addComponent(
        eff.described("CHANGE RES",
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

