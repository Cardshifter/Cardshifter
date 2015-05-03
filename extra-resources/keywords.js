var keywords = {};
keywords.cards = {};
keywords.cards.name = function (entity, obj, value) {
    java.lang.System.out.println("before apply");
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.NAME, value);
    java.lang.System.out.println("after apply");
}
keywords.cards.flavor = function (entity, obj, value) {
    com.cardshifter.modapi.attributes.ECSAttributeMap.createOrGetFor(entity).set(com.cardshifter.modapi.attributes.FLAVOR, value);
}

function applyEntity(game, card, entity, keyword) {
    java.lang.System.out.println("applyEntity " + card + ": " + entity);

    for (var property in card) {
        if (card.hasOwnProperty(property)) {
            var value = card[property];
            java.lang.System.out.println("property found: " + property + " with value " + value + " keyword data is " + keyword[property]);
            if (keyword[property] === undefined) {
                java.lang.System.out.println("keyword " + property + " is undefined");
                throw new Error("property " + property + " was found but is not a declared keyword");
            }
            keyword[property].call(entity, card, value);
        }
    }

}


function applyCardKeywords(game, zone, data) {

    for (var i = 0; i < data.cards.length; i++) {
        var card = data.cards[i];
        var entity = game.newEntity();
        applyEntity(game, card, entity, keywords.cards);
        zone.addOnBottom(entity);
    }
}

function applyKeywords(game, data) {

    beforeApplyKeywords(game, data);
    applyKeywords(game, data);
    afterApplyKeywords(game, data);

}