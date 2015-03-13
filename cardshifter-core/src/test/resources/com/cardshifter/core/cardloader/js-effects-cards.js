function getCards() {
    return [
        {
            name: "Ragnaros",
            attack: 8,
            hitpoints: 8,
            onPhaseEnd: function (game, event) {
                if (event.getOldPhase().getName() === "Main") {
                    game.opponent().characters().pickRandom(1).dealDamage(8);
                }
            }
        }
    ]
}