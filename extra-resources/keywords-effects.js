/**
 * Contains the various card effect types and the properties associated thereof,
 *  and the logic to build card descriptions from said properties.
 * @module keywords-effects
 */
"use strict";

keywords.effects.print = {
    /** Build action description */
    description: function (obj) {
        print("calling description: " + obj);
        return "print " + obj.message.length + " characters";
    },
    /**
     * Prints a message to the console. Mostly used for debugging.
     *  Has no effect on the game behavior at all. 
     * @param {Object} obj - The applicable card object along with related properties/values.
     */
    action: function (obj) {
        return function (me, event) {
            print("PrintEffect: " + me + " message: " + obj.message);
        }
    }
};

keywords.effects.damage = {
    /** Build action description */
    description: function(obj) {
        return "Deal " + valueDescription(obj.value) + " damage to " + obj.target;
    },
    /**
     * Deals damage to a target.
     * @param {Object} obj - The applicable card object along with related properties/values.
     */
    action: function (obj) {
        return function (me, event) {
            var target = entityLookup(me, obj.target);
            var value = valueLookup(me, obj.value);
            if (value < 0) {
                throw new Error("Damage value cannot be negative, was " + value);
            }
            HEALTH.retriever.resFor(target).change(-value);
        }
    }
};

keywords.effects.heal = {
    /** Build action description */
    description: function(obj) {
        return "Heal " + valueDescription(obj.value) + " damage to " + obj.target;
    },
    /**
     * Heals damage from a target. 
     * @param {Object} obj - The applicable card object along with related properties/values.
     */
    action: function (obj) {
        return function (me, event) {
            var target = entityLookup(me, obj.target);
            var value = valueLookup(me, obj.value);
            if (value < 0) {
                throw new Error("Heal value cannot be negative, was " + value);
            }
            HEALTH.retriever.resFor(target).change(value);
        }
    }
};

keywords.effects.summon = {
    /** Build action description */ 
    description: function(obj) {
        return "Summon " + valueDescription(obj.count) + " " + obj.card + " at " + obj.who + " " + obj.where;
    },
    /**
     * Summon other card(s) to the battlefield. 
     * @param {Object} obj - The applicable card object along with related properties/values.
     */
    action: function (obj) {
        return function (me, event) {
            var target = entityLookup(me, obj.who);
            var zone = zoneLookup(target, obj.where);
            var count = valueLookup(me, obj.count);
            if (count < 0) {
                throw new Error("count cannot be negative, was " + count);
            }
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
};
