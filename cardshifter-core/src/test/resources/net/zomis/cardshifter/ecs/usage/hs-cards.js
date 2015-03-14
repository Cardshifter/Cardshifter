function getCards() {
    return [
        {
            name: "Murloc Raider",
            attack: 2,
            hitpoints: 1,
            cost: 1,
            race: "Murloc",
            type: "Minion"
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
    for (var property in card) {
        if (!card.hasOwnProperty(property)) {
            continue;
        }
        if (typeof card[property] !== "function") {
            continue;
        }


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

    //setup if type is Minion
    if (card.type === "Minion") {
        if (!card.hasOwnProperty("attack_available")) {
            card.attack_available = 1;
        }
        if (!card.hasOwnProperty("sickness")) {
            card.sickness = 1;
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