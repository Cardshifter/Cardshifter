/**
 * Defines filters applicable to effect keywords. 
 * @module keywords-filter
 */

keywords.filters.owner = {
    /** 
     * Build owner filter description.
     * @param value {Object} - The applicable card object along with related properties/values.
     */ 
    description: function (value) {
        return "owned by " + value;
    },
    /**
     * Declares applicable owner filter.
     * @param entity {Object} - The applicable card entity.
     * @param filter {string} - The declared filter.
     */
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
        throw new Error("Unknown filter value for owner: " + filter);
    }
};

keywords.filters.zone = {
    /** 
     * Build zone filter description.
     * @param value {Object} - The applicable card object along with related properties/values.
     */ 
    description: function (value) {
        return "on " + value;
    },
    /**
     * Declares applicable zone filter.
     * @param entity {Object} - The applicable card entity.
     * @param filter {string} - The zone declared by the filter.
     */
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
    /** 
     * Build creature filter description.
     * @param value {Object} - The applicable card object along with related properties/values.
     */
    description: function (value) {
        return "creature";
    },
    /**
     * Declares applicable creature filter.
     * @param entity {Object} - The applicable card entity.
     * @param filter {string} - The creature declared by the filter.
     */
    func: function (entity, filter) {
        return function (source, target) {
            return target.hasComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class);
        }
    }
};

keywords.filters.creatureType = {
    /** 
     * Build creatureType filter description
     * @param value {Object} - The applicable card object along with related properties/values.
     */
    description: function (value) {
        return value + " creatures";
    },
    /**
     * Declares applicable creatureType filter.
     * @param entity {Object} - The applicable card entity.
     * @param filter {string} - The creatureType declared by the filter.
     */
    func: function (entity, filter) {
        return function (source, target) {
            var comp = target.getComponent(com.cardshifter.modapi.base.CreatureTypeComponent.class);
            return comp !== null && filter === comp.creatureType;
        }
    }
};

/**
 * 
 * @param entity {Object} - Applicable card entity.
 * @param filter {Object} - Applicable card object.
 * @returns {Function} - Returns if the source and target make a valid function. 
 */
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
            var func = keywords.filters[property].func(entity, value);
            if (func === undefined) {
                throw new Error("Filter failure: " + filter);
            }
            functions.push(func);
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

