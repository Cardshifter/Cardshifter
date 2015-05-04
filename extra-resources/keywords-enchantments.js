keywords.cards.enchantment = function (entity, obj, value) {
    if (obj.creature) {
        throw new Error("cannot be both enchantment and creature at once");
    }
    var actions = new com.cardshifter.modapi.actions.ActionComponent();
    var enchantAction = new ECSAction(entity, ENCHANT_ACTION, function (act) { return true; }, function (act) {}).addTargetSet(1, 1);
    actions.addAction(enchantAction);
	entity.addComponent(actions);
}

keywords.cards.scrapCost = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    SCRAP_COST.retriever.set(entity, value);
}

keywords.cards.addAttack = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    ATTACK.retriever.set(entity, value);
}

keywords.cards.addHealth = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    HEALTH.retriever.set(entity, value);
    MAX_HEALTH.retriever.set(entity, value);
}
