"use strict";
var ECSAction = Java.type("com.cardshifter.modapi.actions.ECSAction");
var keywords = {};
keywords.cards = {};
keywords.afterCards = [];
keywords.moreSystems = [];

function requireActions(actions) {
    for (var i = 0; i < actions.length; i++) {
        var type = typeof actions[i];
        if (type !== 'string') {
            throw new Error("A required action constant was not found: index " + i + ", expected String but was " + type);
        }
    }
}

function createResource(name) {
    return new com.cardshifter.modapi.resources.ECSResourceDefault(name);
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
    var cardEntities = [];
    for (var i = 0; i < data.cards.length; i++) {
        var card = data.cards[i];
        var entity = game.newEntity();
        applyEntity(game, card, entity, keywords.cards);
        zone.addOnBottom(entity);
        cardEntities.push(entity);
    }
    for (var i = 0; i < keywords.afterCards.length; i++) {
        keywords.afterCards[i](game, data, cardEntities);
    }
}

function applySystem(game, data, keyword) {
    print("applySystem " + data);

    for (var property in data) {
        if (data.hasOwnProperty(property)) {
            var value = data[property];
            print("property found: " + property + " with value " + value + " keyword data is " + keyword[property]);
            if (keyword[property] === undefined) {
                print("keyword " + property + " is undefined");
                throw new Error("property " + property + " was found but is not a declared keyword");
            }
            var system = keyword[property].call(null, game, data, value);
            game.addSystem(system);
        }
    }
}

function applySystems(game, data) {
    for (var i = 0; i < keywords.moreSystems.length; i++) {
        keywords.moreSystems[i](game, data);
    }

    for (var i = 0; i < data.length; i++) {
        var system = data[i];
        if (system instanceof com.cardshifter.modapi.base.ECSSystem) {
            game.addSystem(system);
        } else {
            applySystem(game, system, keywords.systems);
        }
    }
}
