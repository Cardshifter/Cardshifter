package net.zomis.cardshifter.ecs.usage;

import java.util.function.UnaryOperator;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.UseCostSystem;
import com.cardshifter.modapi.actions.attack.AttackDamageYGO;
import com.cardshifter.modapi.actions.attack.AttackOnBattlefield;
import com.cardshifter.modapi.actions.attack.AttackSickness;
import com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer;
import com.cardshifter.modapi.actions.enchant.EnchantPerform;
import com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.cards.DamageConstantWhenOutOfCardsSystem;
import com.cardshifter.modapi.cards.DeckComponent;
import com.cardshifter.modapi.cards.DrawCardAtBeginningOfTurnSystem;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.LimitedHandSizeSystem;
import com.cardshifter.modapi.cards.MulliganSingleCards;
import com.cardshifter.modapi.cards.PlayEntersBattlefieldSystem;
import com.cardshifter.modapi.cards.PlayFromHandSystem;
import com.cardshifter.modapi.cards.RemoveDeadEntityFromZoneSystem;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.phase.GainResourceSystem;
import com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.phase.RestoreResourcesSystem;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.GameOverIfNoHealth;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.cardshifter.modapi.resources.RestoreResourcesToSystem;

public class PhrancisGame implements ECSMod {

	public enum PhrancisResources implements ECSResource {
		HEALTH, MANA, MANA_MAX, SCRAP, ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE;
	}

	public static final String PLAY_ACTION = "Play";
	public static final String ENCHANT_ACTION = "Enchant";
	public static final String ATTACK_ACTION = "Attack";
	public static final String SCRAP_ACTION = "Scrap";
	public static final String END_TURN_ACTION = "End Turn";
	
	private static final int CARDS_OF_EACH_TYPE = 3;
	
	@Override
	public void declareConfiguration(ECSGame game) {
		Entity neutral = game.newEntity();
		ZoneComponent zone = new ZoneComponent(neutral, "Cards");
		
		// Create card models that should be possible to choose from
		
		createCreature(1, zone, 1, 1, "B0T", 1);
		createCreature(2, zone, 2, 1, "B0T", 1);
		createCreature(3, zone, 3, 3, "B0T", 1);
		createCreature(4, zone, 4, 4, "B0T", 1);
		createCreature(5, zone, 5, 5, "B0T", 1);
		
		createCreature(5, zone, 4, 4, "Bio", 0);
		createCreature(5, zone, 5, 3, "Bio", 0);
		createCreature(5, zone, 3, 5, "Bio", 0);
		
		createEnchantment(zone, 1, 0, 1);
		createEnchantment(zone, 0, 1, 1);
		createEnchantment(zone, 3, 0, 3);
		createEnchantment(zone, 0, 3, 3);
		createEnchantment(zone, 2, 2, 5);
		
		// Create the players
		
		for (int i = 0; i < 2; i++) {
			Entity entity = game.newEntity();
			PlayerComponent playerComponent = new PlayerComponent(i, "Player" + (i+1));
			entity.addComponent(playerComponent);
			DeckConfig config = new DeckConfig(30, 30, zone.getCards(), 3);
			entity.addComponent(new ConfigComponent().addConfig("Deck", config));
		}
		
	}
	
	@Deprecated
	public static ECSGame createGame(ECSGame game) {
		new PhrancisGame().setupGame(game);
		return game;
	}
	
	@Override
	public void setupGame(ECSGame game) {
		
		PhaseController phaseController = new PhaseController();
		game.newEntity().addComponent(phaseController);
		
		for (int i = 0; i < 2; i++) {
			final int playerIndex = i;
			Entity player = game.findEntities(e -> e.hasComponent(PlayerComponent.class) && e.getComponent(PlayerComponent.class).getIndex() == playerIndex).get(0);
			Phase playerPhase = new Phase(player, "Main");
			phaseController.addPhase(playerPhase);
			
			ActionComponent actions = new ActionComponent();
			player.addComponent(actions);
			
			ECSAction endTurnAction = new ECSAction(player, END_TURN_ACTION, act -> phaseController.getCurrentPhase() == playerPhase, act -> phaseController.nextPhase());
			actions.addAction(endTurnAction);
			
			ECSResourceMap.createFor(player)
				.set(PhrancisResources.HEALTH, 10)
				.set(PhrancisResources.MANA, 0)
				.set(PhrancisResources.SCRAP, 0);
			
			ZoneComponent deck = new DeckComponent(player);
			ZoneComponent hand = new HandComponent(player);
			ZoneComponent battlefield = new BattlefieldComponent(player);
			player.addComponents(hand, deck, battlefield);
			
			ConfigComponent config = player.getComponent(ConfigComponent.class);
			DeckConfig deckConf = config.getConfig(DeckConfig.class);
			
			for (int card = 0; card < CARDS_OF_EACH_TYPE; card++) {
				createCreature(1, deck, 1, 1, "B0T", 1);
				createCreature(2, deck, 2, 1, "B0T", 1);
				createCreature(3, deck, 3, 3, "B0T", 1);
				createCreature(4, deck, 4, 4, "B0T", 1);
				createCreature(5, deck, 5, 5, "B0T", 1);
				
				createCreature(5, deck, 4, 4, "Bio", 0);
				createCreature(5, deck, 5, 3, "Bio", 0);
				createCreature(5, deck, 3, 5, "Bio", 0);
				
				createEnchantment(deck, 1, 0, 1);
				createEnchantment(deck, 0, 1, 1);
				createEnchantment(deck, 3, 0, 3);
				createEnchantment(deck, 0, 3, 3);
				createEnchantment(deck, 2, 2, 5);
			}
			deck.shuffle();
		}
		
		ResourceRetriever manaMaxResource = ResourceRetriever.forResource(PhrancisResources.MANA_MAX);
		ResourceRetriever manaCostResource = ResourceRetriever.forResource(PhrancisResources.MANA_COST);
		UnaryOperator<Entity> owningPlayerPays = entity -> entity.getComponent(CardComponent.class).getOwner();
		game.addSystem(new GainResourceSystem(PhrancisResources.MANA_MAX, entity -> Math.min(1, Math.abs(manaMaxResource.getFor(entity) - 10))));
		game.addSystem(new RestoreResourcesSystem(PhrancisResources.MANA, entity -> manaMaxResource.getFor(entity)));
		
		// Actions - Play
		game.addSystem(new PlayFromHandSystem(PLAY_ACTION));
		game.addSystem(new PlayEntersBattlefieldSystem(PLAY_ACTION));
		game.addSystem(new UseCostSystem(PLAY_ACTION, PhrancisResources.MANA, manaCostResource::getFor, owningPlayerPays));
		
		// Actions - Scrap
		ResourceRetriever scrapCostResource = ResourceRetriever.forResource(PhrancisResources.SCRAP_COST);
		game.addSystem(new ScrapSystem(PhrancisResources.SCRAP));
		
		// Actions - Attack
		game.addSystem(new AttackOnBattlefield());
		game.addSystem(new AttackSickness(PhrancisResources.SICKNESS));
		game.addSystem(new AttackTargetMinionsFirstThenPlayer());
		game.addSystem(new AttackDamageYGO(PhrancisResources.ATTACK, PhrancisResources.HEALTH));
		game.addSystem(new UseCostSystem(ATTACK_ACTION, PhrancisResources.ATTACK_AVAILABLE, entity -> 1, entity -> entity));
		game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class) 
				&& Cards.isOnZone(entity, BattlefieldComponent.class), PhrancisResources.ATTACK_AVAILABLE, entity -> 1));
		game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class)
				&& Cards.isOnZone(entity, BattlefieldComponent.class), PhrancisResources.SICKNESS, entity -> 0));
		
		// Actions - Enchant
		game.addSystem(new PlayFromHandSystem(ENCHANT_ACTION));
		game.addSystem(new UseCostSystem(ENCHANT_ACTION, PhrancisResources.SCRAP, scrapCostResource::getFor, owningPlayerPays));
		game.addSystem(new EnchantTargetCreatureTypes(new String[]{ "Bio" }));
		game.addSystem(new EnchantPerform(PhrancisResources.ATTACK, PhrancisResources.HEALTH));
		
//		game.addSystem(new ConsumeCardSystem());
//		game.addSystem(new LimitedPlaysPerTurnSystem(2));
		
		// Resources
		// TODO: ManaOverloadSystem -- Uses an `OverloadComponent` for both cards and players. Checks for turn start and afterCardPlayed
		
		// Draw cards
		game.addSystem(new DrawStartCards(5));
		game.addSystem(new MulliganSingleCards(game));
		game.addSystem(new DrawCardAtBeginningOfTurnSystem());
		game.addSystem(new DamageConstantWhenOutOfCardsSystem(PhrancisResources.HEALTH, 1));
//		game.addSystem(new DamageIncreasingWhenOutOfCardsSystem());
		game.addSystem(new LimitedHandSizeSystem(10, card -> card.getCardToDraw().destroy()));
//		game.addSystem(new RecreateDeckSystem());
		
		// Initial setup
		// TODO: ??? Card models aren't being used the same way now... game.addSystem(new DeckFromEachCardSystem(4, null));
//		game.addSystem(new CreateDeckOnceFromSourceSystem());
		// TODO: game.addSystem(new GiveStartCard(game.getPlayers().get(1), "The Coin"));
		
		// General setup
		game.addSystem(new GameOverIfNoHealth(PhrancisResources.HEALTH));
		game.addSystem(new RemoveDeadEntityFromZoneSystem());
		game.addSystem(new PerformerMustBeCurrentPlayer());
		
	}

	private static Entity createEnchantment(ZoneComponent deck, int strength, int health, int cost) {
		Entity entity = deck.getOwner().getGame().newEntity();
		ECSResourceMap.createFor(entity)
			.set(PhrancisResources.HEALTH, health)
			.set(PhrancisResources.SCRAP_COST, cost)
			.set(PhrancisResources.ATTACK, strength);
		entity.addComponent(new ActionComponent().addAction(enchantAction(entity)));
		deck.addOnBottom(entity);
		return entity;
	}

	private static ECSAction enchantAction(Entity entity) {
		return new ECSAction(entity, ENCHANT_ACTION, act -> true, act -> {}).addTargetSet(1, 1);
	}

	private static Entity createCreature(int cost, ZoneComponent deck, int strength,
			int health, String creatureType, int scrapValue) {
		Entity entity = deck.getOwner().getGame().newEntity();
		ECSResourceMap.createFor(entity)
			.set(PhrancisResources.HEALTH, health)
			.set(PhrancisResources.ATTACK, strength)
			.set(PhrancisResources.SCRAP, scrapValue)
			.set(PhrancisResources.MANA_COST, cost)
			.set(PhrancisResources.SICKNESS, 1)
			.set(PhrancisResources.ATTACK_AVAILABLE, 1);
		entity.addComponent(new CreatureTypeComponent(creatureType));
		deck.addOnBottom(entity);
		
		ActionComponent actions = new ActionComponent();
		entity.addComponent(actions);
		
		actions.addAction(playAction(entity));
		actions.addAction(attackAction(entity));
		actions.addAction(scrapAction(entity));
		
		return entity;
	}
	
	private static ECSAction attackAction(Entity entity) {
		return new ECSAction(entity, ATTACK_ACTION, act -> true, act -> {}).addTargetSet(1, 1);
	}

	private static ECSAction scrapAction(Entity entity) {
		return new ECSAction(entity, SCRAP_ACTION, act -> true, act -> {});
	}

	private static ECSAction playAction(Entity entity) {
		return new ECSAction(entity, PLAY_ACTION, act -> true, act -> {});
	}
	
}
