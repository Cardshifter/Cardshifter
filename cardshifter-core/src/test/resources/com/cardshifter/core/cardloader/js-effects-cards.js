function getCards() {
    return [
        {
            name: "Ragnaros",
            attack: 8,
            hitpoints: 8,
            onMainPhaseEnd: function (game, event) {
                game.opponent().characters().pickRandom(1).dealDamage(8);
            }
        }
    ].map(mapCard);
}

function mapCard(card) {
    //use regex to match property on on*PhaseEnd
    for (var property in card) {
        if (!card.hasOwnProperty(property)) {
            continue;
        }
        if (typeof card[property] !== "function") {
            continue;
        }

        var functions = [];
        var saveObject = {};

        var matches = property.match(/^on(.*)PhaseEnd$/);
        if (matches) {
            var phaseName = matches[1];
            saveObject[property] = card[property];
            delete card[property];
            functions.push((function (phaseName, property) {
                return function (game, event) {
                    if (event.getOldPhase().getName() === phaseName) {
                        saveObject[property](game, event);
                    }
                }
            })(phaseName, property));
        }
    }

    if (card.onPhaseEnd) {
        functions.push(card.onPhaseEnd);
    }
    card.onPhaseEnd = function(game, event) {
        for (var i = 0; i < functions.length; i++) {
            functions[i](game, event);
        }
    }

    return card;
}