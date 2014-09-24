package net.zomis.cardshifter.ecs.usage;

import java.util.function.UnaryOperator;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.actions.attack.AttackDamageYGO;
import net.zomis.cardshifter.ecs.actions.attack.AttackTargetMinionsFirstThenPlayer;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.Cards;
import net.zomis.cardshifter.ecs.cards.DeckComponent;
import net.zomis.cardshifter.ecs.cards.DrawStartCards;
import net.zomis.cardshifter.ecs.cards.HandComponent;
import net.zomis.cardshifter.ecs.cards.LimitedHandSizeSystem;
import net.zomis.cardshifter.ecs.cards.RemoveDeadEntityFromZoneSystem;
import net.zomis.cardshifter.ecs.cards.ZoneComponent;
import net.zomis.cardshifter.ecs.components.CreatureTypeComponent;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.phase.Phase;
import net.zomis.cardshifter.ecs.phase.PhaseController;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;
import net.zomis.cardshifter.ecs.resources.RestoreResourcesToSystem;
import net.zomis.cardshifter.ecs.systems.AttackOnBattlefield;
import net.zomis.cardshifter.ecs.systems.AttackSickness;
import net.zomis.cardshifter.ecs.systems.DamageConstantWhenOutOfCardsSystem;
import net.zomis.cardshifter.ecs.systems.DrawCardAtBeginningOfTurnSystem;
import net.zomis.cardshifter.ecs.systems.GainResourceSystem;
import net.zomis.cardshifter.ecs.systems.GameOverIfNoHealth;
import net.zomis.cardshifter.ecs.systems.UseCostSystem;
import net.zomis.cardshifter.ecs.systems.PlayEntersBattlefieldSystem;
import net.zomis.cardshifter.ecs.systems.PlayFromHandSystem;
import net.zomis.cardshifter.ecs.systems.RestoreResourcesSystem;

public class PhrancisGame {

	public enum PhrancisResources implements ECSResource {
		HEALTH, MANA, MANA_MAX, SCRAP, ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE;
	}

	public static final String PLAY_ACTION = "Play";
	public static final String ENCHANT_ACTION = "Enchant";
	public static final String ATTACK_ACTION = "Attack";
	public static final String SCRAP_ACTION = "Scrap";
	public static final String END_TURN_ACTION = "End Turn";
	
	private static final int CARDS_OF_EACH_TYPE = 3;
	private static final int BOT_CARDS = 5;
	
	public static ECSGame createGame() {
		ECSGame game = new ECSGame();
		
		PhaseController phaseController = new PhaseController();
		game.newEntity().addComponent(phaseController);
		
		for (int i = 1; i <= 2; i++) {
			PlayerComponent playerComponent = new PlayerComponent(i - 1, "Player" + i);
			Entity player = game.newEntity().addComponent(playerComponent);
			Phase playerPhase = new Phase(player, "Main");
			phaseController.addPhase(playerPhase);
			
			ActionComponent actions = new ActionComponent();
			player.addComponent(actions);
			actions.addAction(new ECSAction(player, END_TURN_ACTION, act -> phaseController.getCurrentPhase() == playerPhase, act -> phaseController.nextPhase()));
			
			ECSResourceMap.createFor(player)
				.set(PhrancisResources.HEALTH, 10)
				.set(PhrancisResources.MANA, 0)
				.set(PhrancisResources.SCRAP, 0);
			
			ZoneComponent deck = new DeckComponent(player);
			ZoneComponent hand = new HandComponent(player);
			ZoneComponent battlefield = new BattlefieldComponent(player);
			player.addComponents(hand, deck, battlefield);
			
			for (int card = 0; card < CARDS_OF_EACH_TYPE; card++) {
				
				for (int strength = 1; strength <= BOT_CARDS; strength++) {
					Entity cardEntity = createCreature(deck, strength, strength, strength, "B0T");
					if (strength == 2) {
						cardEntity.getComponent(ECSResourceMap.class).getResource(PhrancisResources.HEALTH).change(-1);
					}
				}
				createCreature(deck, 5, 4, 4, "Bio");
				createCreature(deck, 5, 5, 3, "Bio");
				createCreature(deck, 5, 3, 5, "Bio");
				
				createEnchantment(deck, 1, 0, 1);
				createEnchantment(deck, 0, 1, 1);
				createEnchantment(deck, 3, 0, 3);
				createEnchantment(deck, 0, 3, 3);
				createEnchantment(deck, 2, 2, 5);

			}
			deck.shuffle();
		}
		
		ResourceRetriever manaMaxResource = ResourceRetriever.forResource(PhrancisResources.MANA_MAX);
		ResourceRetriever manaResource = ResourceRetriever.forResource(PhrancisResources.MANA);
		manaMaxResource.resFor(phaseController.getCurrentEntity()).change(1);
		manaResource.resFor(phaseController.getCurrentEntity()).change(1);
		
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
		
		return game;
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

	private static Entity createCreature(ZoneComponent deck, int cost,
			int strength, int health, String creatureType) {
		Entity entity = deck.getOwner().getGame().newEntity();
		ECSResourceMap.createFor(entity)
			.set(PhrancisResources.HEALTH, health)
			.set(PhrancisResources.ATTACK, strength)
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
