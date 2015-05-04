keywords.cards.enchantment = function (entity, obj, value) {
    if (obj.creature) {
        throw new Error("cannot be both enchantment and creature at once");
    }
    var actions = new com.cardshifter.modapi.actions.ActionComponent();
    var pg = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
    actions.addAction(pg.enchantAction(entity));
	entity.addComponent(actions);
}

keywords.cards.scrapCost = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.SCRAP_COST, value);
}

keywords.cards.addAttack = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.ATTACK, value);
}

keywords.cards.addHealth = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.HEALTH, value);
	com.cardshifter.modapi.resources.ECSResourceMap.createOrGetFor(entity).set(pgres.MAX_HEALTH, value);
}
