var noAttackCreatures = [];

keywords.cards = {};
keywords.cards.name = function (entity, obj, value) {
    print("before apply: " + entity);
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.Attributes.NAME, value);
    print("after apply");
}
keywords.cards.flavor = function (entity, obj, value) {
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.Attributes.FLAVOR, value);
}
keywords.cards.creature = function (entity, obj, value) {
    var actions = new com.cardshifter.modapi.actions.ActionComponent();
	entity.addComponent(actions);

    var playAction = new ECSAction(entity, PLAY_ACTION, function (act) { return true; }, function (act) {});
    var attackAction = new ECSAction(entity, ATTACK_ACTION, function (act) { return true; }, function (act) {}).addTargetSet(1, 1);
    var scrapAction = new ECSAction(entity, SCRAP_ACTION, function (act) { return true; }, function (act) {});

	actions.addAction(playAction);
	actions.addAction(attackAction);
	actions.addAction(scrapAction);

	entity.addComponent(new com.cardshifter.modapi.base.CreatureTypeComponent(value));

	var map = com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity);
	map.set(SICKNESS, 1);
	map.set(TAUNT, 1);
	map.set(ATTACK_AVAILABLE, 1);

}

keywords.cards.manaCost = function (entity, obj, value) {
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(MANA_COST, value);
}

keywords.cards.health = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(HEALTH, value);
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(MAX_HEALTH, value);
}

keywords.cards.attack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(ATTACK, value);
}

keywords.cards.scrap = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(SCRAP, value);
}

keywords.cards.sickness = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(SICKNESS, value);
}

keywords.cards.noAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	noAttackCreatures.push(obj.name);
}

keywords.cards.denyCounterAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(DENY_COUNTERATTACK, value);
}
