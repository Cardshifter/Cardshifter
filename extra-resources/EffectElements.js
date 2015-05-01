var effectElements = {
    triggers: {
        onEndTurn: function() {},
        onStartTurn: function() {},
        onStartGame: function() {},

        onSelfPlayerDamage: function() {},
        onOpponentPlayerDamage: function() {},
        onAnyPlayerDamage: function() {},

        onSelfCardDamage: function() {},
        onSelfCardDeath: function() {},
        onOpponentCardDamage: function() {},
        onOpponentCardDeath: function() {},
        onSelfSpecificCardDeath: function() {},
        onOpponentSpecificCardDeath: function() {}
    },
    actions: {
        damage: function() {},
        heal: function() {},
        drawCard: function() {},
        burnCard: function() {},

        addAttack: function() {},
        addHealth: function() {},

        giveRush: function() {},
        giveRanged: function() {},
        giveTaunt: function() {},
        giveWait: function() {}
    },
    targets: {
        selfPlayer: function() {},
        opponentPlayer: function() {},
        allPlayers: function() {},

        selfCards: function() {},
        opponentCards: function() {},
        allCards: function() {},

        selfDeck: function() {},
        opponentDeck: function() {},
        allDecks: function() {},

        selfBoard: function() {},
        opponentBoard: function() {},
        allBoards: function() {}
    },
    others: {
        chance: function(probability) {
            if (probability > 1) {
                probability = 1;
            }
            if (probability < 0) {
                probability = 0;
            }
            if ((typeof probability) !== "number" && isNaN(probability)) {
                return NaN;
            } else {
                var roll = Math.random();
                return (roll <= probability);
            }
        }
    }
};
