"use strict";
var ECSAction = Java.type("com.cardshifter.modapi.actions.ECSAction");
var Players = Java.type("com.cardshifter.modapi.players.Players");
var Cards = Java.type("com.cardshifter.modapi.cards.Cards");
var ComponentRetriever = Java.type("com.cardshifter.modapi.base.ComponentRetriever");
var keywords = {};
keywords.cards = {};
keywords.afterCards = [];
keywords.moreSystems = [];
keywords.effects = {};

function resolveFilter(entity, filter) {
    return function (source, target) {
        var result = true;
        if (filter.samePlayer) {
            result = result && Players.findOwnerFor(source) == Players.findOwnerFor(target);
        }
        if (filter.zone) {
            var card = ComponentRetriever.retreiverFor(com.cardshifter.modapi.cards.CardComponent.class);
            var targetCard = card.get(target);
            if (!targetCard) {
                return false;
            }
            result = result && targetCard.currentZone.name === filter.zone;
        }
        if (filter.creature) {
            result = result && target.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class);
        }
        return result;
    };
}

function resolveModifier(entity, data) {
    var priority = data.priority;
    var res = data.res;

    var change = data.change;
    var appliesTo = resolveFilter(entity, data.filter);
    var active = function (entity) {
        return !entity.removed;
    };
    var amount = function (source, target, resource, actualValue) {
        return actualValue + data.change;
    };

    print("EntityModifier " + entity + " prio " + priority + " applies " + appliesTo + " amount " + amount);
    var obj = new com.cardshifter.modapi.resources.EntityModifier(entity, priority, active, appliesTo, amount);
    return obj;

}

function resolveModifiers(entity, data) {
    var result = [];
    for (var i = 0; i < data.length; i++) {
        var modifierData = data[i];

        var modifier = resolveModifier(entity, modifierData);
        result.push({ res: modifierData.res, object: modifier });
    }
    return result;
}

function zoneLookup(entity, zoneName) {
    return entity.getSuperComponents(com.cardshifter.modapi.cards.ZoneComponent.class).stream()
        .filter(function(zone) { return zone.name.equals(zoneName); }).findAny()
        .orElseThrow(function() {
            return new java.lang.IllegalStateException("No zone found with name '" + zoneName + "' on " + entity);
        });
}

function valueDescription(value) {
    if (typeof value === 'number') {
        return value;
    } else if (typeof value === 'object') {
        if (value.func && value.description) {
            return "X (where X is " + value.description + ")";
        }
        return value.min + " to " + value.max;
    } else {
        throw new Error("Unknown type for value: " + value + " with type " + typeof value);
    }
}

function valueLookup(entity, value) {
    if (typeof value === 'number') {
        return value;
    } else if (typeof value === 'object') {
        if (value.func && value.description) {
            return value.func(entity);
        }
        return entity.game.randomRange(value.min, value.max);
    } else {
        throw new Error("Unknown type for value: " + value + " with type " + typeof value);
    }
}

function entityLookup(origin, who) {
    if (who === 'owner') {
        return Players.findOwnerFor(origin);
    }
    if (who === 'opponent') {
        return Players.getNextPlayer(Players.findOwnerFor(origin));
    }
    if (who === 'this') {
        return origin;
    }
    throw new Error("unexpected target for entity lookup: " + who);
}

function applyEffect(obj) {
    print("applyEffect " + obj);

    var result = null;

    for (var property in obj) {
        if (obj.hasOwnProperty(property)) {
            var value = obj[property];
            print("property found: " + property + " with value " + value + " keyword data is " + keywords.effects[property]);
            if (keywords.effects[property] === undefined) {
                print("keyword " + property + " is undefined");
                throw new Error("property " + property + " was found but is not a declared keyword");
            }
            if (result !== null) {
                throw new Error("currently only supporting one effect");
            }
            result = {};
            result.description = keywords.effects[property].description(value);
            result.action = keywords.effects[property].action(value);
        }
    }
    return result;
}

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
