function getCards() {
    return [
        {
            data: {
                name: "Ragnaros",
                attack: 8,
                hitpoints: 8,
                keywords: ["cant_attack"]
            },
            events: {
                BattlefieldComponent: {
                    onMainPhaseEnd: function (game, event) {
                        game.opponent().characters().pickRandom(1).dealDamage(8);
                    }
                }
            },
            setupEntity: function (entity) {
                var ActionComponent = Java.type("com.cardshifter.modapi.actions.ActionComponent");
                var actionComponent = new ActionComponent();
                entity.addComponent(actionComponent);
            }
        }
    ].map(mapCard);
}

function mapCard(card) {
    //use regex to match property on on*PhaseEnd
    var functions = [];
    var saveObject = {};
    for (var component in card.events) {
        if (!card.events.hasOwnProperty(component)) {
            continue;
        }

        saveObject[component] = {};
        for (var property in card.events[component]) {
            if (!card.events[component].hasOwnProperty(property)) {
                continue;
            }
            if (typeof card.events[component][property] !== "function") {
                continue;
            }

            var matches = property.match(/^on(.*)PhaseEnd$/);
            if (matches) {
                var phaseName = matches[1];
                saveObject[component][property] = card.events[component][property];
                delete card.events[component][property];
                functions.push((function (phaseName, property, component) {
                    return function (game, event) {
                        if (event.getOldPhase().getName() === phaseName) {
                            saveObject[component][property](game, event);
                        }
                    }
                })(phaseName, property, component));
            }
        }

        if (card.events[component].onPhaseEnd) {
            functions.push(card.events[component].onPhaseEnd);
        }
        card.events[component].onPhaseEnd = function(game, event) {
            for (var i = 0; i < functions.length; i++) {
                functions[i](game, event);
            }
        }
    }


    //resolve keywords array
    if (card.data.keywords) {
        for (var i = 0; i < card.data.keywords.length; i++) {
            card.data[card.data.keywords[i]] = 1;
        }
        delete card.data.keywords;
    }

    return card;
}