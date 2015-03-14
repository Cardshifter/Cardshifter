package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.core.cardloader.CardLoadingException;
import com.cardshifter.core.cardloader.JsEffectsCardLoader;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.UseCostSystem;
import com.cardshifter.modapi.actions.attack.AttackDamageYGO;
import com.cardshifter.modapi.actions.attack.AttackOnBattlefield;
import com.cardshifter.modapi.actions.attack.AttackSickness;
import com.cardshifter.modapi.actions.attack.TrampleSystem;
import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.ModDSL;
import com.cardshifter.modapi.base.ModHelper;
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
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import net.zomis.cardshifter.ecs.config.DeckConfigFactory;
import net.zomis.cardshifter.ecs.effects.EffectActionSystem;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class HearthstoneGame implements ECSMod {

	public enum HearthstoneResources implements ECSResource {
		ATTACK, HEALTH, DAMAGE, MANA, MANA_MAX, COST, SICKNESS, ATTACK_AVAILABLE, DENY_COUNTERATTACK;
	}

	public enum HearthstoneAttributes implements ECSAttribute {
		NAME, TYPE, RACE;
	}

	public static final String PLAY_ACTION = "Play";
	public static final String ATTACK_ACTION = "Attack";
	public static final String END_TURN_ACTION = "End Turn";

	private static final AttributeRetriever name = AttributeRetriever.forAttribute(HearthstoneAttributes.NAME);
	private final Set<String> noAttackNames = new HashSet<>();
	private Consumer<Entity> noAttack = e -> noAttackNames.add(name.getFor(e));
	private Entity neutral;

	@Override
	public void declareConfiguration(ECSGame game) {
		neutral = game.newEntity();
		ZoneComponent zone = new ZoneComponent(neutral, "Cards");
		neutral.addComponent(zone);
		addCards(zone);

		// Create the players
		int maxCardsPerType = 30;
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

		Path cardFile = ModHelper.getPath(this, "hs-cards.js");
		try {
			Collection<Entity> cards = new JsEffectsCardLoader().loadCards(cardFile, zone.getComponentEntity().getGame(), this, HearthstoneResources.values(), HearthstoneAttributes.values());
			cards.forEach(c -> zone.addOnBottom(c));
		} catch (CardLoadingException e) {
			throw new RuntimeException(e);
		}

		ECSGame game = zone.getComponentEntity().getGame();
		game.addSystem(new DenyActionForNames(HearthstoneGame.ATTACK_ACTION, noAttackNames));
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
				.set(HearthstoneResources.HEALTH, 30)
				.set(HearthstoneResources.MANA, 0);

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

		ResourceRetriever manaMaxResource = ResourceRetriever.forResource(HearthstoneResources.MANA_MAX);
		ResourceRetriever costResource = ResourceRetriever.forResource(HearthstoneResources.COST);
		UnaryOperator<Entity> owningPlayerPays = entity -> entity.getComponent(CardComponent.class).getOwner();
		game.addSystem(new GainResourceSystem(HearthstoneResources.MANA_MAX, entity -> Math.min(1, Math.abs(manaMaxResource.getFor(entity) - 10))));
		game.addSystem(new RestoreResourcesSystem(HearthstoneResources.MANA, entity -> manaMaxResource.getFor(entity)));

		// Actions - Play
		game.addSystem(new PlayFromHandSystem(PLAY_ACTION));
		game.addSystem(new PlayEntersBattlefieldSystem(PLAY_ACTION));
		game.addSystem(new UseCostSystem(PLAY_ACTION, HearthstoneResources.MANA, costResource::getFor, owningPlayerPays));

		// Actions - Scrap
		ResourceRetriever sickness = ResourceRetriever.forResource(HearthstoneResources.SICKNESS);

		// Actions - Spell
		game.addSystem(new EffectActionSystem(PLAY_ACTION));

		// Actions - Attack
		ResourceRetriever allowCounterAttackRes = ResourceRetriever.forResource(HearthstoneResources.DENY_COUNTERATTACK);
		BiPredicate<Entity, Entity> allowCounterAttack =
			(attacker, defender) -> allowCounterAttackRes.getOrDefault(attacker, 0) == 0;
		game.addSystem(new AttackOnBattlefield());
		game.addSystem(new AttackSickness(HearthstoneResources.SICKNESS));
//		game.addSystem(new AttackTargetMinionsFirstThenPlayer(HearthstoneResources.TAUNT));
		game.addSystem(new AttackDamageYGO(HearthstoneResources.ATTACK, HearthstoneResources.HEALTH, allowCounterAttack));
		game.addSystem(new UseCostSystem(ATTACK_ACTION, HearthstoneResources.ATTACK_AVAILABLE, entity -> 1, entity -> entity));
		game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class)
			&& Cards.isOnZone(entity, BattlefieldComponent.class)
			&& Cards.isOwnedByCurrentPlayer(entity), HearthstoneResources.ATTACK_AVAILABLE, entity -> 1));
		game.addSystem(new RestoreResourcesToSystem(entity -> entity.hasComponent(CreatureTypeComponent.class)
			&& Cards.isOnZone(entity, BattlefieldComponent.class)
			&& Cards.isOwnedByCurrentPlayer(entity), HearthstoneResources.SICKNESS,
			entity -> Math.max(0, sickness.getFor(entity) - 1)));
		game.addSystem(new TrampleSystem(HearthstoneResources.HEALTH));
		game.addSystem(new ApplyAfterAttack(e -> allowCounterAttackRes.getFor(e) > 0, e -> sickness.resFor(e).set(2)));

		// Resources
		// TODO: ManaOverloadSystem -- Uses an `OverloadComponent` for both cards and players. Checks for turn start and afterCardPlayed

		// Draw cards
		game.addSystem(new DrawStartCards(5));
		game.addSystem(new MulliganSingleCards(game));
		game.addSystem(new DrawCardAtBeginningOfTurnSystem());
		game.addSystem(new DamageConstantWhenOutOfCardsSystem(HearthstoneResources.HEALTH, 1));
		game.addSystem(new LimitedHandSizeSystem(10, card -> card.getCardToDraw().destroy()));

		// Initial setup
		// TODO: game.addSystem(new GiveStartCard(game.getPlayers().get(1), "The Coin"));

		// General setup
		game.addSystem(new GameOverIfNoHealth(HearthstoneResources.HEALTH));
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

	private static ECSAction attackAction(Entity entity) {
		return new ECSAction(entity, ATTACK_ACTION, act -> true, act -> {}).addTargetSet(1, 1);
	}

	private static ECSAction playAction(Entity entity) {
		return new ECSAction(entity, PLAY_ACTION, act -> true, act -> {});
	}

	@Override
	public Class<? extends ModDSL> dslClass() {
		return HearthstoneDSL.class;
	}

	public static class HearthstoneDSL implements ModDSL {

	}
}
