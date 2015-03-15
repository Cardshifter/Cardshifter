function getCards() {
    return [
        {
            data: {
                name: "Murloc Raider",
                attack: 2,
                hitpoints: 1,
                cost: 1,
                race: "Murloc",
                type: "Minion"
            }
        },
        {
            data: {
                name: "Ragnaros",
                attack: 8,
                hitpoints: 8,
                cost: 8,
                type: "Minion",
                attack_available: 0
            },
            events: {
                battlefield: {
                    onMainPhaseEnd: function (game, event) {
                        game.opponent().characters().pickRandom(1).dealDamage(8);
                    }
                }
            }
        }
    ].map(mapCard);
}

var ActionComponent = Java.type("com.cardshifter.modapi.actions.ActionComponent");
var ECSAction = Java.type("com.cardshifter.modapi.actions.ECSAction");
var HearthstoneGame = Java.type("net.zomis.cardshifter.ecs.usage.HearthstoneGame");

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

    //rewrite zone names
    if (card.events.battlefield) {
        card.events.BattlefieldComponent = card.events.battlefield;
        delete card.events.battlefield;
    }

    //setup if type is Minion
    if (card.data.type === "Minion") {
        if (!card.hasOwnProperty("attack_available")) {
            card.data.attack_available = 1;
        }
        if (!card.hasOwnProperty("sickness")) {
            card.data.sickness = 1;
        }

        card.setupEntity = function (entity) {
            var actionComponent = new ActionComponent();
            entity.addComponent(actionComponent);

            actionComponent.addAction(playAction(entity));
            actionComponent.addAction(attackAction(entity));
        };
    }

    return card;
}

function playAction(entity) {
    return new ECSAction(
        entity,
        HearthstoneGame.PLAY_ACTION,
        function (action) {
            return true;
        },
        function (action) {  }
    );
}

function attackAction(entity) {
    return new ECSAction(
        entity,
        HearthstoneGame.ATTACK_ACTION,
        function (action) {
            return true;
        },
        function (action) {  }
    ).addTargetSet(1, 1);
}