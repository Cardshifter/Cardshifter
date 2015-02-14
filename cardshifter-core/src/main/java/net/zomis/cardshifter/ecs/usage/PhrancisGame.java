package net.zomis.cardshifter.ecs.usage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.cardshifter.core.cardloader.CardLoadingException;
import com.cardshifter.core.cardloader.SimpleCardLoader;
import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.*;
import com.cardshifter.modapi.phase.*;
import com.cardshifter.modapi.players.Players;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import com.cardshifter.api.config.DeckConfig;
import net.zomis.cardshifter.ecs.config.DeckConfigFactory;
import net.zomis.cardshifter.ecs.effects.*;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.UseCostSystem;
import com.cardshifter.modapi.actions.attack.AttackDamageYGO;
import com.cardshifter.modapi.actions.attack.AttackOnBattlefield;
import com.cardshifter.modapi.actions.attack.AttackSickness;
import com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer;
import com.cardshifter.modapi.actions.attack.TrampleSystem;
import com.cardshifter.modapi.actions.enchant.EnchantPerform;
import com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
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
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.GameOverIfNoHealth;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.cardshifter.modapi.resources.RestoreResourcesToSystem;

public class PhrancisGame implements ECSMod {

	public enum PhrancisResources implements ECSResource {
		MAX_HEALTH,
		SNIPER,
		DOUBLE_ATTACK,
		TAUNT,
		DENY_COUNTERATTACK,
		HEALTH, MANA, MANA_MAX, SCRAP, ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE;
	}

	public static final String PLAY_ACTION = "Play";
	public static final String ENCHANT_ACTION = "Enchant";
	public static final String ATTACK_ACTION = "Attack";
	public static final String SCRAP_ACTION = "Scrap";
	public static final String END_TURN_ACTION = "End Turn";
	public static final String USE_ACTION = "Use";
	
	@Override
	public void declareConfiguration(ECSGame game) {
		Entity neutral = game.newEntity();
		ZoneComponent zone = new ZoneComponent(neutral, "Cards");
		neutral.addComponent(zone);
		addCards(zone);
		
		// Create the players
		int maxCardsPerType = 3;
		int minSize = 30;
		int maxSize = 30;
		
		for (int i = 0; i < 2; i++) {
			Entity entity = game.newEntity();
			PlayerComponent playerComponent = new PlayerComponent(i, "Player" + (i+1));
			entity.addComponent(playerComponent);
			DeckConfig config = DeckConfigFactory.create(minSize, maxSize, zone.getCards(), maxCardsPerType);
			entity.addComponent(new ConfigComponent().addConfig("Deck", config));
		}
		
	}
	
	public void addCards(ZoneComponent zone) {
		// Create card models that should be possible to choose from
		ResourceRetriever sickness = ResourceRetriever.forResource(PhrancisResources.SICKNESS);
		ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
		ResourceRetriever healthMax = ResourceRetriever.forResource(PhrancisResources.MAX_HEALTH);
		Consumer<Entity> noSickness = e -> sickness.resFor(e).set(0);
		BiFunction<Entity, Integer, Integer> restoreHealth = (e, value) ->
				Math.max(Math.min(healthMax.getFor(e) - health.getFor(e), value), 0);
		AttributeRetriever name = AttributeRetriever.forAttribute(Attributes.NAME);
		Set<String> noAttackNames = new HashSet<>();
		Consumer<Entity> noAttack = e -> noAttackNames.add(name.getFor(e));

		ResourceRetriever rangedResource = ResourceRetriever.forResource(PhrancisResources.DENY_COUNTERATTACK);
		Consumer<Entity> ranged = e -> rangedResource.resFor(e).set(1);

		Path cardFile = ModHelper.getPath(this, "phrancis-cards.cards");
		ECSAttribute[] defaultAttributes = new ECSAttribute[]{ Attributes.NAME, Attributes.FLAVOR };
		try {
			Collection<Entity> cards = new SimpleCardLoader().loadCards(cardFile, zone.getComponentEntity().getGame(),
					this, PhrancisResources.values(), defaultAttributes);
			cards.forEach(c -> zone.addOnBottom(c));
		} catch (CardLoadingException e) {
			throw new RuntimeException(e);
		}

		// Mechs (ManaCost, zone, Attack, Health, "Type", ScrapValue, "CardName")
//		createCreature(0, zone, 0, 1, "Mech", 3, "Spareparts");
		createCreature(1, zone, 1, 1, "Mech", 1, "Gyrodroid").apply(ranged);
		createCreature(2, zone, 2, 1, "Mech", 1, "The Chopper");
		createCreature(2, zone, 1, 2, "Mech", 1, "Shieldmech");
		createCreature(3, zone, 3, 3, "Mech", 2, "Humadroid");
		createCreature(3, zone, 4, 2, "Mech", 2, "Assassinatrix").apply(ranged);
		createCreature(3, zone, 2, 4, "Mech", 2, "Fortimech");
		createCreature(3, zone, 5, 1, "Mech", 2, "Scout Mech").apply(noSickness);
		createCreature(3, zone, 0, 5, "Mech", 3, "Supply Mech").apply(noAttack);
		createCreature(5, zone, 5, 3, "Mech", 3, "Modleg Ambusher").apply(noSickness);
		createCreature(5, zone, 3, 6, "Mech", 3, "Heavy Mech");
		createCreature(5, zone, 4, 4, "Mech", 3, "Waste Runner");

		// Bios(ManaCost, zone, Attack, Health, "Type", ScrapValue, "CardName")
		createCreature(2, zone, 2, 2, "Bio", 0, "Conscript");
		createCreature(3, zone, 3, 2, "Bio", 0, "Longshot").apply(ranged);
		Entity bodyMan = createCreature(4, zone, 2, 3, "Bio", 0, "Bodyman");
		createCreature(5, zone, 3, 3, "Bio", 0, "Vetter");
		Effects effects = new Effects();
		createCreature(5, zone, 1, 5, "Bio", 0, "Field Medic").addComponent(effects.described("Heal 1 at end of turn", effects.giveSelf(
			effects.triggerSystem(PhaseEndEvent.class,
			(me, event) -> Players.findOwnerFor(me) == event.getOldPhase().getOwner(),
			(me, event) -> Players.findOwnerFor(me).apply(e -> health.resFor(e).change(restoreHealth.apply(e, 1)))))));
		createCreature(6, zone, 4, 4, "Bio", 0, "Wastelander");
		createCreature(6, zone, 5, 3, "Bio", 0, "Commander").apply(noSickness);
		createCreature(6, zone, 3, 5, "Bio", 0, "Cyberpimp");
		createCreature(7, zone, 5, 5, "Bio", 0, "Cyborg");
		createCreature(8, zone, 6, 6, "Bio", 0, "Web Boss");
		createCreature(8, zone, 2, 6, "Bio", 0, "Inside Man").apply(noAttack).addComponent(effects.described("Summon 2 BodyMans", effects.toSelf(e -> {
			Entity entity = Players.findOwnerFor(e);
			ZoneComponent field = entity.getComponent(BattlefieldComponent.class);
			field.addOnBottom(bodyMan.copy());
			field.addOnBottom(bodyMan.copy());
		})));

		// Enchantments: (zone, AttackModify, HealthModify, ScrapCost, "CardName")
		createEnchantment(zone, 2, 0, 1, "Bionic Arms");
		createEnchantment(zone, 0, 2, 1, "Body Armor");
		createEnchantment(zone, 1, 1, 1, "Adrenalin Injection")
			.addComponent(effects.described("Give Rush", effects.giveTarget(PhrancisResources.SICKNESS, 0, i -> 0)));
		createEnchantment(zone, 2, 1, 2, "Steroid Implants");
		createEnchantment(zone, 1, 2, 2, "Reinforced Cranial Implants");
		createEnchantment(zone, 3, 0, 2, "Cybernetic Arm Cannon")
			.addComponent(effects.described("Give Ranged", effects.giveTarget(PhrancisResources.DENY_COUNTERATTACK, 1)));

		createEnchantment(zone, 0, 3, 2, "Exoskeleton");
		createEnchantment(zone, 2, 2, 3, "Artificial Intelligence Implants");
		createEnchantment(zone, 3, 3, 5, "Full-body Cybernetics Upgrade");

		ECSGame game = zone.getComponentEntity().getGame();
		game.addSystem(new DenyActionForNames(PhrancisGame.ATTACK_ACTION, noAttackNames));
	}

	public Entity createTargetSpell(String name, ZoneComponent zone, int manaCost, int scrapCost, EffectComponent effect, FilterComponent filter) {
		return createSpellWithTargets(name, 1, zone, manaCost, scrapCost, effect, filter);
	}

	public Entity createSpell(String name, ZoneComponent zone, int manaCost, int scrapCost, EffectComponent effect) {
		return createSpellWithTargets(name, 0, zone, manaCost, scrapCost, effect);
	}

	private Entity createSpellWithTargets(String name, int targets, ZoneComponent zone, int manaCost, int scrapCost, Component... components) {
		Entity entity = zone.getOwner().getGame().newEntity();
		ECSResourceMap.createFor(entity)
			.set(PhrancisResources.SCRAP_COST, scrapCost)
			.set(PhrancisResources.MANA_COST, manaCost);
		ECSAttributeMap.createFor(entity).set(Attributes.NAME, name);
		entity.addComponent(new ActionComponent().addAction(spellAction(entity, targets)));
		entity.addComponents(components);
		zone.addOnBottom(entity);
		return entity;
	}

	private ECSAction spellAction(Entity entity, int targets) {
		ECSAction action = new ECSAction(entity, USE_ACTION, act -> true, act -> {});
		if (targets > 0) {
			action.addTargetSet(targets, targets);
		}
		return action;
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
				.set(PhrancisResources.HEALTH, 30)
				.set(PhrancisResources.MAX_HEALTH, 30)
				.set(PhrancisResources.MANA, 0)
				.set(PhrancisResources.SCRAP, 0);
			
			ZoneComponent deck = new DeckComponent(player);
			ZoneComponent hand = new HandComponent(player);
			ZoneComponent battlefield = new BattlefieldComponent(player);
			player.addComponents(hand, deck, battlefield);
			
			ConfigComponent config = player.getComponent(ConfigComponent.class);
			DeckConfig deckConf = config.getConfig(DeckConfig.class);
			if (deckConf.total() < deckConf.getMinSize()) {
				deckConf.generateRandom();
			}
			
			setupDeck(deck, deckConf);
			
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
		
		// Actions - Spell
		game.addSystem(new UseCostSystem(USE_ACTION, PhrancisResources.MANA, manaCostResource::getFor, owningPlayerPays));
		game.addSystem(new UseCostSystem(USE_ACTION, PhrancisResources.SCRAP, scrapCostResource::getFor, owningPlayerPays));
		game.addSystem(new PlayFromHandSystem(USE_ACTION));
		game.addSystem(new EffectActionSystem(USE_ACTION));
		game.addSystem(new EffectActionSystem(ENCHANT_ACTION));
		game.addSystem(new EffectActionSystem(PLAY_ACTION));
		game.addSystem(new EffectTargetFilterSystem(USE_ACTION));
		game.addSystem(new DestroyAfterUseSystem(USE_ACTION));
		
		// Actions - Attack
		ResourceRetriever allowCounterAttackRes = ResourceRetriever.forResource(PhrancisResources.DENY_COUNTERATTACK);
		BiPredicate<Entity, Entity> allowCounterAttack =
				(attacker, defender) -> allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
		game.addSystem(new AttackOnBattlefield());
		game.addSystem(new AttackSickness(PhrancisResources.SICKNESS));
		game.addSystem(new AttackTargetMinionsFirstThenPlayer(PhrancisResources.TAUNT));
		game.addSystem(new AttackDamageYGO(PhrancisResources.ATTACK, PhrancisResources.HEALTH, allowCounterAttack));
		game.addSystem(new UseCostSystem(ATTACK_ACTION, PhrancisResources.ATTACK_AVAILABLE, entity -> 1, entity -> entity));
		game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class) 
				&& Cards.isOnZone(entity, BattlefieldComponent.class), PhrancisResources.ATTACK_AVAILABLE, entity -> 1));
		game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class)
				&& Cards.isOnZone(entity, BattlefieldComponent.class), PhrancisResources.SICKNESS, entity -> 0));
		game.addSystem(new TrampleSystem(PhrancisResources.HEALTH));
		
		// Actions - Enchant
		game.addSystem(new PlayFromHandSystem(ENCHANT_ACTION));
		game.addSystem(new UseCostSystem(ENCHANT_ACTION, PhrancisResources.SCRAP, scrapCostResource::getFor, owningPlayerPays));
		game.addSystem(new EnchantTargetCreatureTypes(new String[]{ "Bio" }));
		game.addSystem(new EnchantPerform(PhrancisResources.ATTACK, PhrancisResources.HEALTH, PhrancisResources.MAX_HEALTH));
		
//		game.addSystem(new ConsumeCardSystem());
		
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
//		game.addSystem(new CreateDeckOnceFromSourceSystem());
		// TODO: game.addSystem(new GiveStartCard(game.getPlayers().get(1), "The Coin"));
		
		// General setup
		game.addSystem(new GameOverIfNoHealth(PhrancisResources.HEALTH));
		game.addSystem(new LastPlayersStandingEndsGame());
		game.addSystem(new RemoveDeadEntityFromZoneSystem());
		game.addSystem(new PerformerMustBeCurrentPlayer());
		
	}

	private void setupDeck(ZoneComponent deck, DeckConfig deckConf) {
		ECSGame game = deck.getOwner().getGame();
		for (Entry<Integer, Integer> chosen : deckConf.getChosen().entrySet()) {
			int entityId = chosen.getKey();
			int count = chosen.getValue();
			
			for (int i = 0; i < count; i++) {
				Entity existing = game.getEntity(entityId);
				Entity copy = existing.copy();
				deck.addOnBottom(copy);
			}
		}
	}

	public Entity createEnchantment(ZoneComponent deck, int strength, int health, int cost, String name) {
		Entity entity = deck.getOwner().getGame().newEntity();
		ECSResourceMap.createFor(entity)
			.set(PhrancisResources.HEALTH, health)
			.set(PhrancisResources.MAX_HEALTH, health)
			.set(PhrancisResources.SCRAP_COST, cost)
			.set(PhrancisResources.ATTACK, strength);
		ECSAttributeMap.createFor(entity).set(Attributes.NAME, name);
		entity.addComponent(new ActionComponent().addAction(enchantAction(entity)));
		deck.addOnBottom(entity);
		return entity;
	}

	private Consumer<Entity> creature(String creatureType) {
		return entity -> {
			ActionComponent actions = new ActionComponent();
			entity.addComponent(actions);

			actions.addAction(playAction(entity));
			actions.addAction(attackAction(entity));
			actions.addAction(scrapAction(entity));

			entity.addComponent(new CreatureTypeComponent(creatureType));

			ECSResourceMap map = ECSResourceMap.createOrGetFor(entity);
			map.set(PhrancisResources.SICKNESS, 1);
			map.set(PhrancisResources.TAUNT, 1);
//		map.set(PhrancisResources.TRAMPLE, 1);
			map.set(PhrancisResources.ATTACK_AVAILABLE, 1);
		};
	}

	private Consumer<Entity> health(int health) {
		return e -> {
			ECSResourceMap map = ECSResourceMap.createOrGetFor(e);
			map.set(PhrancisResources.HEALTH, health);
			map.set(PhrancisResources.MAX_HEALTH, health);
		};
	}

	private static ECSAction enchantAction(Entity entity) {
		return new ECSAction(entity, ENCHANT_ACTION, act -> true, act -> {}).addTargetSet(1, 1);
	}

	public Entity createCreature(int cost, ZoneComponent deck, int strength,
			int health, String creatureType, int scrapValue) {
		return createCreature(cost, deck, strength, health, creatureType, scrapValue, "Untitled");
	}
	
	public Entity createCreature(int cost, ZoneComponent deck, int strength,
			int health, String creatureType, int scrapValue, String name) {
		Entity entity = deck.getOwner().getGame().newEntity();
		ECSResourceMap.createFor(entity)
			.set(PhrancisResources.HEALTH, health)
			.set(PhrancisResources.MAX_HEALTH, health)
			.set(PhrancisResources.ATTACK, strength)
			.set(PhrancisResources.SCRAP, scrapValue)
			.set(PhrancisResources.MANA_COST, cost)
			.set(PhrancisResources.SICKNESS, 1)
			.set(PhrancisResources.TAUNT, 1)
//			.set(PhrancisResources.TRAMPLE, 1)
			.set(PhrancisResources.ATTACK_AVAILABLE, 1);
		ECSAttributeMap.createFor(entity).set(Attributes.NAME, name);
		creature(creatureType).accept(entity);
		deck.addOnBottom(entity);
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
