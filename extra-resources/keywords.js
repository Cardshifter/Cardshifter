var pgres = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame").PhrancisResources;
var keywords = {};

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