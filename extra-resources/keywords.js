var keywords = {};
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

function applyEntity(game, card, entity, keyword) {
    print("applyEntity " + card + ": " + entity);

    for (var property in card) {
        if (card.hasOwnProperty(property)) {
            var value = card[property];
            print("property found: " + property + " with value " + value + " keyword data is " + keyword[property]);
            if (keyword[property] === undefined) {
                print("keyword " + property + " is undefined");
                throw new Error("property " + property + " was found but is not a declared keyword");
            }
            keyword[property].call(null, entity, card, value);
        }
    }

}


function applyCardKeywords(game, zone, data) {

    for (var i = 0; i < data.cards.length; i++) {
        var card = data.cards[i];
        var entity = game.newEntity();
        print("entity is " + entity);
        applyEntity(game, card, entity, keywords.cards);
        zone.addOnBottom(entity);
    }
}

function applyKeywords(game, data) {

    beforeApplyKeywords(game, data);
    applyKeywords(game, data);
    afterApplyKeywords(game, data);

}