"use strict";
keywords.systems = {};
keywords.systems.gainResource = function (game, data, value) {
    var retriever = com.cardshifter.modapi.resources.ResourceRetriever.forResource(value.res);
    return new com.cardshifter.modapi.phase.GainResourceSystem(value.res, function (entity) {
        return Math.min(1, Math.max(0, value.untilMax - retriever.getFor(entity)));
    });
};
keywords.systems.restoreResources = function (game, data, value) {
    var retriever = com.cardshifter.modapi.resources.ResourceRetriever.forResource(value.res);
    return new com.cardshifter.modapi.phase.RestoreResourcesSystem(value.res, function (entity) {
        if (typeof value.value === 'object') {
            var restoreTo = com.cardshifter.modapi.resources.ResourceRetriever.forResource(value.value.res);
            return restoreTo.getFor(entity);
        } else {
            return value.value;
        }
    });
};
keywords.systems.playFromHand = function (game, data, value) {
    return new com.cardshifter.modapi.cards.PlayFromHandSystem(value);
};
keywords.systems.playEntersBattlefield = function (game, data, value) {
    return new com.cardshifter.modapi.cards.PlayEntersBattlefieldSystem(value);
};
keywords.systems.useCost = function (game, data, value) {
    var retriever = com.cardshifter.modapi.resources.ResourceRetriever.forResource(value.res);
    return new com.cardshifter.modapi.actions.UseCostSystem(value.action, value.res, function (entity) {
        if (typeof value.value === 'object') {
            var restoreTo = com.cardshifter.modapi.resources.ResourceRetriever.forResource(value.value.res);
            return restoreTo.getFor(entity);
        } else {
            return value.value;
        }
    }, function (entity) {
        // who pays
        if (value.whoPays === 'player') {
            return com.cardshifter.modapi.players.Players.findOwnerFor(entity);
        } else if (value.whoPays === 'self') {
            return entity;
        } else throw new Error("unknown value for 'whoPays': " + value.whoPays);
    });
};
keywords.systems.startCards = function (game, data, value) {
    return new com.cardshifter.modapi.cards.DrawStartCards(value);
}
keywords.systems.targetFilterSystem = function (game, data, value) {
    var type = Java.type("net.zomis.cardshifter.ecs.effects.EffectTargetFilterSystem");
    return new type(value);
}
keywords.systems.destroyAfterUse = function (game, data, value) {
    var type = Java.type("net.zomis.cardshifter.ecs.usage.DestroyAfterUseSystem");
    return new type(value);
}
