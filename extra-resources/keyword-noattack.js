var noAttackCreatures = new java.util.HashSet();

keywords.cards.noAttack = function (entity, obj, value) {
    if (!obj.creature) {
        throw new Error("expected creature");
    }
	noAttackCreatures.add(obj.name);
}

keywords.afterCards.push(function (game, cardData, cardEntities) {
    var System = Java.type("net.zomis.cardshifter.ecs.usage.DenyActionForNames");
	game.addSystem(new System(ATTACK_ACTION, noAttackCreatures));
});
