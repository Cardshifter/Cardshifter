load('keywords.js');
load('keywords-creatures.js');
load('keywords-enchantments.js');

/**
 * Declare game configuration
 * @param {Object} game - Game configuration data
 */
function declareConfiguration(game) {
	var neutral = game.newEntity();
	var zone = new com.cardshifter.modapi.cards.ZoneComponent(neutral, "Cards");
	neutral.addComponent(zone);
	addCards(game, zone);

	/** Parameters related to DeckConfigFactory */
	var maxCardsPerType = 5;
	var minSize = 10;
	var maxSize = 10;

	/**
	 * Create playerComponent 0 & 1, i.e., Player1 & Player2
	 * Config a deck for each player
	 */
	for (var i = 0; i < 2; i++) {
		var entity = game.newEntity();
		var playerComponent = new com.cardshifter.modapi.base.PlayerComponent(i, "Player" + (i+1));
		entity.addComponent(playerComponent);
		var config = Java.type("net.zomis.cardshifter.ecs.config.DeckConfigFactory").create(minSize, maxSize, zone.getCards(), maxCardsPerType);
		var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
		entity.addComponent(new ConfigComponent().addConfig("Deck", config));
	}
}


/**
 * Contains the library of cards available for a Cardshifter game in JSON format.
 * Note: Please use the 'effects' wrapper only for properties that change the *behavior* of a card.
 *   Keep regular/arbitrary attributes outside of effects to keep things organized.
 *
 * @module CardData
 */
function addCards(game, zone) {
    print("addCards called");


    applyCardKeywords(game, zone, {
        cards: [
            /** MECH CREATURES */
            {
                name: "Spareparts",
                flavor: "Cobbled together from whatever was lying around at the time.",
                creature: "Mech",
                manaCost: 0,
                health: 1,
                attack: 0,
                scrap: 3,
                sickness: 0,
                noAttack: true
            },
            /** BIO CREATURES */
            {
                name: "Longshot",
                flavor: "Eyes and reflexes augmented for maximum deadliness.",
                creature: "Bio",
                manaCost: 3,
                health: 1,
                attack: 3,
                denyCounterAttack: 1
            },
            /** ENCHANTMENTS */
            {
                name: "Bionic Arms",
                flavor: "These arms will give strength to even the most puny individual.",
                enchantment: true,
                scrapCost: 1,
                addAttack: 2,
                addHealth: 0
            }
        ]
    });
}

function setupGame(game) {
    var pg = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
    new pg().setupGame(game);
}
/*
function setupGame2(game) {

    var phaseController = new com.cardshifter.modapi.phase.PhaseController();
    game.newEntity().addComponent(phaseController);

    var players = com.cardshifter.modapi.players.Players.getPlayersInGame(game);
    for (var i = 0; i < 2; i++) {
        var playerIndex = i;
        Entity player = players.get(i);
        Phase playerPhase = new Phase(player, "Main");
        phaseController.addPhase(playerPhase);

        var actions = new com.cardshifter.modapi.actions.ActionComponent();
        player.addComponent(actions);

        ECSAction endTurnAction = new ECSAction(player, END_TURN_ACTION, function (act) {
            return phaseController.getCurrentPhase() == playerPhase;
        }, function (act) {
            phaseController.nextPhase();
        });
        actions.addAction(endTurnAction);


        com.cardshifter.modapi.resources.ECSResourceMap.createFor(player)
            .set(pgres.HEALTH, 30)
            .set(pgres.MAX_HEALTH, 30)
            .set(pgres.MANA, 0)
            .set(pgres.SCRAP, 0);

        var deck = new com.cardshifter.modapi.cards.DeckComponent(player);
        var hand = new com.cardshifter.modapi.cards.HandComponent(player);
        var battlefield = new com.cardshifter.modapi.cards.BattlefieldComponent(player);
        player.addComponents(hand, deck, battlefield);

        var config = player.getComponent(net.zomis.cardshifter.ecs.config.ConfigComponent.class);
        var deckConf = config.getConfig(com.cardshifter.api.config.DeckConfig.class);
        if (deckConf.total() < deckConf.getMinSize()) {
            deckConf.generateRandom();
        }

        setupDeck(deck, deckConf);

        deck.shuffle();
    }

    var manaMaxResource = ResourceRetriever.forResource(PhrancisResources.MANA_MAX);
    var manaCostResource = ResourceRetriever.forResource(PhrancisResources.MANA_COST);
    UnaryOperator<Entity> owningPlayerPays = entity -> entity.getComponent(CardComponent.class).getOwner();
    game.addSystem(new GainResourceSystem(PhrancisResources.MANA_MAX, entity -> Math.min(1, Math.abs(manaMaxResource.getFor(entity) - 10))));
    game.addSystem(new RestoreResourcesSystem(PhrancisResources.MANA, entity -> manaMaxResource.getFor(entity)));

    // Actions - Play
    game.addSystem(new com.cardshifter.modapi.cards.PlayFromHandSystem(PLAY_ACTION));
    game.addSystem(new com.cardshifter.modapi.cards.PlayEntersBattlefieldSystem(PLAY_ACTION));
    game.addSystem(new UseCostSystem(PLAY_ACTION, PhrancisResources.MANA, manaCostResource::getFor, owningPlayerPays));

    // Actions - Scrap
    ResourceRetriever scrapCostResource = ResourceRetriever.forResource(PhrancisResources.SCRAP_COST);
    ResourceRetriever attackAvailable = ResourceRetriever.forResource(PhrancisResources.ATTACK_AVAILABLE);
    ResourceRetriever sickness = ResourceRetriever.forResource(PhrancisResources.SICKNESS);
    game.addSystem(new ScrapSystem(PhrancisResources.SCRAP,	e ->
            attackAvailable.getOrDefault(e, 0) > 0 &&
            sickness.getOrDefault(e, 1) == 0
    ));

    // Actions - Spell
    game.addSystem(new com.cardshifter.modapi.actions.UseCostSystem(USE_ACTION, PhrancisResources.MANA, manaCostResource::getFor, owningPlayerPays));
    game.addSystem(new com.cardshifter.modapi.actions.UseCostSystem(USE_ACTION, PhrancisResources.SCRAP, scrapCostResource::getFor, owningPlayerPays));
    game.addSystem(new com.cardshifter.modapi.cards.PlayFromHandSystem(USE_ACTION));
    game.addSystem(new EffectActionSystem(USE_ACTION));
    game.addSystem(new EffectActionSystem(ENCHANT_ACTION));
    game.addSystem(new EffectActionSystem(PLAY_ACTION));
    game.addSystem(new EffectTargetFilterSystem(USE_ACTION));
    game.addSystem(new DestroyAfterUseSystem(USE_ACTION));

    // Actions - Attack
    ResourceRetriever allowCounterAttackRes = ResourceRetriever.forResource(PhrancisResources.DENY_COUNTERATTACK);
    BiPredicate<Entity, Entity> allowCounterAttack =
            (attacker, defender) -> allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackOnBattlefield());
    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackSickness(PhrancisResources.SICKNESS));
    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer(PhrancisResources.TAUNT));
    game.addSystem(new com.cardshifter.modapi.actions.attack.AttackDamageYGO(PhrancisResources.ATTACK, PhrancisResources.HEALTH, allowCounterAttack));
    game.addSystem(new com.cardshifter.modapi.actions.UseCostSystem(ATTACK_ACTION, PhrancisResources.ATTACK_AVAILABLE, entity -> 1, entity -> entity));
    game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class)
            && Cards.isOnZone(entity, BattlefieldComponent.class)
            && Cards.isOwnedByCurrentPlayer(entity), PhrancisResources.ATTACK_AVAILABLE, entity -> 1));
    game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class)
            && Cards.isOnZone(entity, BattlefieldComponent.class)
            && Cards.isOwnedByCurrentPlayer(entity), PhrancisResources.SICKNESS,
            entity -> Math.max(0, sickness.getFor(entity) - 1)));
    game.addSystem(new TrampleSystem(pgres.HEALTH));
    game.addSystem(new ApplyAfterAttack(e -> allowCounterAttackRes.getFor(e) > 0, e -> sickness.resFor(e).set(2)));

    // Actions - Enchant
    game.addSystem(new com.cardshifter.modapi.cards.PlayFromHandSystem(ENCHANT_ACTION));
    game.addSystem(new com.cardshifter.modapi.actions.UseCostSystem(ENCHANT_ACTION, pgres.SCRAP, scrapCostResource::getFor, owningPlayerPays));
    game.addSystem(new com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes(new String[]{ "Bio" }));
    game.addSystem(new com.cardshifter.modapi.actions.enchant.EnchantPerform(pgres.ATTACK, pgres.HEALTH, pgres.MAX_HEALTH));

    // Draw cards
    game.addSystem(new com.cardshifter.modapi.cards.DrawStartCards(5));
    game.addSystem(new com.cardshifter.modapi.cards.MulliganSingleCards(game));
    game.addSystem(new com.cardshifter.modapi.cards.DrawCardAtBeginningOfTurnSystem());
    game.addSystem(new com.cardshifter.modapi.cards.DamageConstantWhenOutOfCardsSystem(PhrancisResources.HEALTH, 1));
    game.addSystem(new com.cardshifter.modapi.cards.LimitedHandSizeSystem(10, card -> card.getCardToDraw().destroy()));

    // General setup
    game.addSystem(new com.cardshifter.modapi.resources.GameOverIfNoHealth(PhrancisResources.HEALTH));
    game.addSystem(new net.zomis.cardshifter.ecs.usage.LastPlayersStandingEndsGame());
    game.addSystem(new com.cardshifter.modapi.cards.RemoveDeadEntityFromZoneSystem());
    game.addSystem(new com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer());



}
*/
