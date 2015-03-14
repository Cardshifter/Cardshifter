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
    //setup if type is Minion
    if (card.type === "Minion") {
        if (!card.attack_available) {
            card.attack_available = 1;
        }
        if (!card.sickness) {
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