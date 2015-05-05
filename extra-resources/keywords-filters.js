keywords.filters.owner = {
    description: function (value) {
        return "owned by " + value;
    },
    func: function (entity, filter) {
        if (filter === "owner") {
            return function (source, target) {
                return Players.findOwnerFor(source) === Players.findOwnerFor(target);
            }
        }
        if (filter === "opponent") {
            return function (source, target) {
                var src = Players.findOwnerFor(source);
                var dst = Players.findOwnerFor(target);
                return (src !== null) && (dst !== null) && (src !== dst);
            }
        }
        if (filter === "next") {
            return function (source, target) {
                return Players.getNextPlayer(Players.findOwnerFor(source)) === Players.findOwnerFor(target);
            }
        }
        if (filter === "none") {
            return function (source, target) {
                return Players.findOwnerFor(target) === null;
            }
        }
        if (filter === "active") {
            return function (source, target) {
                var phaseController = ComponentRetriever.singleton(source.game, com.cardshifter.modapi.phase.PhaseController.class);
                return Players.findOwnerFor(target) === phaseController.currentPhase.owner;
            }
        }
        if (filter === "inactive") {
            return function (source, target) {
                var phaseController = ComponentRetriever.singleton(source.game, com.cardshifter.modapi.phase.PhaseController.class);
                return Players.findOwnerFor(target) !== phaseController.currentPhase.owner;
            }
        }
    }
};

keywords.filters.zone = {
    description: function (value) {
        return "on " + value;
    },
    func: function (entity, filter) {
        return function (source, target) {
            if (!target.hasComponent(com.cardshifter.modapi.cards.CardComponent.class)) {
                return false;
            }
            var card = ComponentRetriever.retreiverFor(com.cardshifter.modapi.cards.CardComponent.class);
            var targetCard = card.get(target);
            return targetCard.currentZone !== null && targetCard.currentZone.name === filter;
        }
    }
};

keywords.filters.creature = {
    description: function (value) {
        return "creature";
    },
    func: function (entity, filter) {
        return function (source, target) {
            return target.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class);
        }
    }
};

keywords.filters.creatureType = {
    description: function (value) {
        return value + " creatures";
    },
    func: function (entity, filter) {
        return function (source, target) {
            return filter === target.getComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class).creatureType;
        }
    }
};

function resolveFilter(entity, filter) {
    var description = "";
    var functions = [];

    for (var property in filter) {
        if (filter.hasOwnProperty(property)) {
            var value = filter[property];
            print("property found: " + property + " with value " + value + " keyword data is " + keywords.effects[property]);
            if (keywords.filters[property] === undefined) {
                print("keyword " + property + " is undefined");
                throw new Error("property " + property + " was found but is not a declared keyword");
            }
            description += keywords.filters[property].description(value);
            functions.push(keywords.filters[property].func(entity, value));
        }
    }
    return function (source, target) {
        for (var i = 0; i < functions.length; i++) {
            var fnc = functions[i](source, target);
            if (!fnc) {
                return false;
            }
        }
        return true;
    };
}

