/**
 * Contains the library of cards available for a Cardshifter game in JSON format.
 * Note: Please use the 'effects' wrapper only for properties that change the *behavior* of a card.
 *   Keep regular/arbitrary attributes outside of effects to keep things organized.
 * 
 * @module CardData
 */


function loadCardLibrary() {
    var cardLibrary = {
        entities: [
            {
                name: "Spareparts",
                flavor: "Cobbled together from whatever was lying around at the time.",
                creature: true,
                creatureType: "Mech",
                manaCost: 0,
                health: 1,
                attack: 0,
                scrap: 3,
                effects: {
                    sickness: 0,
                    noAttack: true
                }
            },
            {
                name: "Longshot",
                flavor: "Eyes and reflexes augmented for maximum deadliness.",
                creature: true,
                creatureType: "Bio",
                manaCost: 3,
                health: 1,
                attack: 3,
                effects: {
                    denyCounterAttack: 1
                }
            },
            {
                name: "Bionic Arms",
                flavor: "These arms will give strength to even the most puny individual.",
                enchantment: true,
                scrapCost: 1,
                effects: {
                    addAttack: 2,
                    addHealth: 0,
                },
            }
        ]
    };
    return cardLibrary;
}

function applyCreature(creatureType) {
    if (creatureType === "Mech") {
        return creatureType;
    } else if (creatureType === "Bio") {
        return creatureType;
    } else {
        console.log("Unknown creature type: " + creatureType);
    }
}

function applyHealth(healthAmount) {
    return healthAmount;
}

function applyNoAttack(hasNoAttack) {
    if (hasNoAttack !== true && hasNoAttack !== false) {
        console.log("applyNoAttack parameter must be either true or false.");
    }
    else {
        return hasNoAttack;
    }
}

/*
var cardLibrary = loadCardLibrary();

console.log(cardLibrary);
console.log(cardLibrary.entities[0].values);
*/
