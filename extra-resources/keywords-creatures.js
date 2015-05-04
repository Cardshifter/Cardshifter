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
    var pg = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
    var actions = new com.cardshifter.modapi.actions.ActionComponent();
	entity.addComponent(actions);

	actions.addAction(pg.playAction(entity));
	actions.addAction(pg.attackAction(entity));
	actions.addAction(pg.scrapAction(entity));

	entity.addComponent(new com.cardshifter.modapi.base.CreatureTypeComponent(value));

	var map = com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity);
	map.set(pg.PhrancisResources.SICKNESS, 1);
	map.set(pg.PhrancisResources.TAUNT, 1);
	map.set(pg.PhrancisResources.ATTACK_AVAILABLE, 1);

}

keywords.cards.manaCost = function (entity, obj, value) {
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.MANA_COST, value);
}

keywords.cards.health = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.HEALTH, value);
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.MAX_HEALTH, value);
}

keywords.cards.attack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.ATTACK, value);
}

keywords.cards.scrap = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.SCRAP, value);
}

keywords.cards.sickness = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.SICKNESS, value);
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
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.DENY_COUNTERATTACK, value);
}

