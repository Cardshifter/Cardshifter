"use strict";
var ECSAction = Java.type("com.cardshifter.modapi.actions.ECSAction");
var Players = Java.type("com.cardshifter.modapi.players.Players");
var keywords = {};
keywords.cards = {};
keywords.afterCards = [];
keywords.moreSystems = [];

keywords.effects = {};

keywords.effects.print = {
    description: function (obj) {
        print("calling description: " + obj);
        return "print " + obj.message.length + " characters";
    },
    action: function (obj) {
        return function (me, event) {
            print("PrintEffect: " + me + " message: " + obj.message);
        }
    }
};
keywords.effects.damage = {
    description: function(obj) {
        return "Deal " + obj.value + " damage to " + obj.target;
    },
    action: function (obj) {
        if (obj.value <= 0) {
            throw new Error("Damage value must be 1 or greater");
        }
        return function (me, event) {
            var target = entityLookup(me, obj.target);
            HEALTH.retriever.resFor(target).change(-obj.value);
        }
    }
}
keywords.effects.heal = {
    description: function(obj) {
        return "Heal " + obj.value + " damage to " + obj.target;
    },
    action: function (obj) {
        if (obj.value <= 0) {
            throw new Error("Heal value must be 1 or greater");
        }
        return function (me, event) {
            var target = entityLookup(me, obj.target);
            HEALTH.retriever.resFor(target).change(obj.value);
        }
    }
}
keywords.effects.summon = {
    description: function(obj) {
        return "Summon " + obj.count + " " + obj.card + " at " + obj.who + " " + obj.where;
    },
    action: function (obj) {
        if (obj.count <= 0) {
            throw new Error("Summon count must be 1 or greater");
        }
        return function (me, event) {
            var target = entityLookup(me, obj.who);
            var zone = zoneLookup(target, obj.where);
            var count = valueLookup(me, obj.count);
            var name = com.cardshifter.modapi.attributes.Attributes.NAME;
            name = com.cardshifter.modapi.attributes.AttributeRetriever.forAttribute(name);

            var neutral = me.getGame().findEntities(function(entity) {
                var comp = entity.getComponent(com.cardshifter.modapi.cards.ZoneComponent.class);
                return (comp !== null) && comp.getName().equals("Cards");
            });
            if (neutral.size() !== 1) {
                throw new Error("Unable to locate the available cards: " + neutral + " size was " + neutral.size);
            }

            var what = neutral.get(0).getComponent(com.cardshifter.modapi.cards.ZoneComponent.class)
                .getCards().stream().filter(function(card) {
                    print("Checking " + card);
                    return name.getFor(card).equals(obj.card);
                }).findAny().get();

            for (var i = 0; i < count; i++) {
                zone.addOnBottom(what.copy());
            }
        }
    }
}

function zoneLookup(entity, zoneName) {
    return entity.getSuperComponents(com.cardshifter.modapi.cards.ZoneComponent.class).stream()
        .filter(function(zone) { return zone.name.equals(zoneName); }).findAny()
        .orElseThrow(function() {
            return new java.lang.IllegalStateException("No zone found with name '" + zoneName + "' on " + entity);
        });
}

function valueLookup(entity, value) {
    return value;
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
