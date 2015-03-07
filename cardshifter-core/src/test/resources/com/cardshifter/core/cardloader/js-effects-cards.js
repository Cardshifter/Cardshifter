function getCards() {
    return [
        {
            name: "Ragnaros",
            attack: 8,
            hitpoints: 8,
            onPhaseEnd: function (game) {
                game.opponent().characters().pickRandom(1).dealDamage(8);
            }
        }
    ]
}