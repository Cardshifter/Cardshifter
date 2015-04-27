function loadCardLibrary() {
    var cardLibrary = {
        entities: [
            {
                name: "Spareparts",
                flavor: "Cobbled together from whatever was lying around at the time.",
                values: {
                    sickness: 0,
                    attack: 0,
                    scrap: 3,
                    manaCost: 0
                },
                apply: [
                    applyCreature("Mech"),
                    applyHealth(1),
                    applyNoAttack(true)
                ]
            },
            {
                name: "Longshot",
                flavor: "Eyes and reflexes augmented for maximum deadliness.",
                values: {
                    DENY_COUNTERATTACK: 1,
                    manaCost: 3,
                    attack: 3
                },
                apply: [
                    applyCreature("Bio"),
                    applyHealth(1)
                ]
            },
            {
                name: "Bionic Arms",
                flavor: "These arms will give strength to even the most puny individual.",
                values: {
                    attack: 2,
                    scrapCost: 1,
                },
                apply: [
                    applyHealth(0)
                ]
            }
        ]
    };
    return cardLibrary;
}
