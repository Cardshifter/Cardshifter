var SCRAP = createResource("SCRAP");
var SCRAP_COST = createResource("SCRAP_COST");
var SCRAP_ACTION = "Scrap";
requireActions([ENCHANT_ACTION, USE_ACTION]);


/**
 * Checks if a card is not a creature, and otherwise set its SCRAP value.
 * @param {Object} entity - The game entity.
 * @param {Object} obj - The applicable card object.
 * @param {int} value - The SCRAP vaule to be applied.
 */
keywords.cards.scrap = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	SCRAP.retriever.set(entity, value);
}

/**
 * Checks if a card is not an enchantment, and otherwise set its SCRAP_COST value.
 * @param {Object} entity - The card entity.
 * @param {Object} obj - The applicable card object.
 * @param {int} value - The SCRAP_COST value to be applied.
 */
keywords.cards.scrapCost = function (entity, obj, value) {
    if (!obj.enchantment) {
        throw new Error("expected enchantment");
    }
    SCRAP_COST.retriever.set(entity, value);
}

/**
 * Applies the SCRAP_ACTION property on cards that can be scrapped.
 * @param {Object} game - The game entity.
 * @param {Object} cards - The applicable cards.
 * @param {Object} cardEntities - The applicable cardEntities.
 */
keywords.afterCards.push(function (game, cards, cardEntities) {
    for each (var entity in cardEntities) {
        if (entity.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class)) {
            actions = entity.getComponent(com.cardshifter.modapi.actions.ActionComponent.class);
            var scrapAction = new ECSAction(entity, SCRAP_ACTION, function (act) { return true; }, function (act) {});
        	actions.addAction(scrapAction);
        }
    }
});

/**
 * Push various scrap-related data to the game system.
 * @param {Object} game - The game entity.
 * @param {Object} systemData - The applicable system data. 
 */
keywords.moreSystems.push(function (game, systemData) {
    // Scrap
    var ScrapSystem = Java.type("net.zomis.cardshifter.ecs.usage.ScrapSystem");
    systemData.push(new ScrapSystem(SCRAP, function (entity) {
        return ATTACK_AVAILABLE.retriever.getOrDefault(entity, 0) > 0
         && SICKNESS.retriever.getOrDefault(entity, 1) == 0;
    }));
    systemData.push({ useCost: { action: ENCHANT_ACTION, res: SCRAP, value: { res: SCRAP_COST }, whoPays: "player" } });
    systemData.push({ useCost: { action: USE_ACTION, res: SCRAP, value: { res: SCRAP_COST }, whoPays: "player" } });
});
