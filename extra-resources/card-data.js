function loadCardLibrary() {
    var cardLibrary = {
        entities: [
            {
                name: "Spareparts",
                flavor: "Cobbled together from whatever was lying around at the time.",
                values: {
                    creature: "Mech",
                    noAttack: true,
                    sickness: 0,
                    manaCost: 0,
                    health: 1,
                    attack: 0,
                    scrap: 3,
                }
            },
            {
                name: "Longshot",
                flavor: "Eyes and reflexes augmented for maximum deadliness.",
                values: {
                    creature: "Bio",
                    denyCounterAttack: 1,
                    manaCost: 3,
                    health: 1,
                    attack: 3
                },
            },
            {
                name: "Bionic Arms",
                flavor: "These arms will give strength to even the most puny individual.",
                values: {
                    addAttack: 2,
                    addHealth: 0,
                    scrapCost: 1,
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
