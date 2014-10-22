package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.zomis.cardshifter.ecs.usage.ConfigComponent;
import net.zomis.cardshifter.ecs.usage.DeckConfig;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.DeckComponent;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class PhrancisTest {

	private static final Logger logger = LogManager.getLogger(PhrancisTest.class);
	
	private ECSGame game;
	private PhaseController phase;
	private final ResourceRetriever mana = ResourceRetriever.forResource(PhrancisResources.MANA);
	private final ResourceRetriever manaCost = ResourceRetriever.forResource(PhrancisResources.MANA_COST);
	private final ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
	private final ResourceRetriever attackPoints = ResourceRetriever.forResource(PhrancisResources.ATTACK_AVAILABLE);
	private final ResourceRetriever scrapCost = ResourceRetriever.forResource(PhrancisResources.SCRAP_COST);
	
	private final ComponentRetriever<ActionComponent> actions = ComponentRetriever.retreiverFor(ActionComponent.class);
	private final ComponentRetriever<DeckComponent> deck = ComponentRetriever.retreiverFor(DeckComponent.class);
	private final ComponentRetriever<HandComponent> hand = ComponentRetriever.retreiverFor(HandComponent.class);
	private final ComponentRetriever<BattlefieldComponent> field = ComponentRetriever.retreiverFor(BattlefieldComponent.class);
	private final ComponentRetriever<CreatureTypeComponent> ctype = ComponentRetriever.retreiverFor(CreatureTypeComponent.class);
	
	private final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	private final Predicate<Entity> isB0T = e -> ctype.has(e) && ctype.get(e).getCreatureType().equals("B0T");
	private final Predicate<Entity> isCreature = entity -> entity.hasComponent(CreatureTypeComponent.class);
			
	@Before
	public void before() {
		PropertyConfigurator.configure(PhrancisTest.class.getResourceAsStream("log4j.properties"));
		game = new ECSGame();
		game.setRandomSeed(42);
		ECSMod mod = new PhrancisGame();
		mod.declareConfiguration(game);
		
		// TODO: Configure decks
		Set<Entity> configEntities = game.getEntitiesWithComponent(ConfigComponent.class);
		for (Entity configEntity : configEntities) {
			ConfigComponent config = configEntity.getComponent(ConfigComponent.class);
			DeckConfig deckConf = config.getConfig(DeckConfig.class);
			
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, e -> scrapCost.getFor(e) == 1);
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, isCreature.and(manaCost(3)));
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, isCreature.and(manaCost(1)));
			addCard(deckConf, isCreatureType("Bio").and(health(4)));
			addCard(deckConf, e -> scrapCost.getFor(e) == 1 && health.getFor(e) == 1);
		}

		mod.setupGame(game);
		
		game.startGame();
		phase = ComponentRetriever.singleton(game, PhaseController.class);
	}
	
	private void addCard(DeckConfig config, Predicate<Entity> condition) {
		Set<Entity> availableCards = game.getEntitiesWithComponent(ZoneComponent.class);
		ZoneComponent zone = availableCards.iterator().next().getComponent(ZoneComponent.class);
		Entity entity = zone.stream().filter(condition).findFirst().get();
		config.add(entity.getId());
	}

	@Test
	@Ignore
	public void integration() {
		assertNull(phase.getCurrentEntity());
		List<Entity> list = new ArrayList<>(game.getEntitiesWithComponent(PlayerComponent.class));
		assertEquals(2, list.size());
		for (int i = 0; i < list.size(); i++) {
			Entity current = list.get(i);
			Entity other = list.get((i + 1) % list.size());
			
			ECSAction action = getAction(current, "Mulligan");
			assertFalse(action + "is allowed for " + other, action.perform(other));
		}
		
		for (Entity entity : game.getEntitiesWithComponent(PlayerComponent.class)) {
			ECSAction action = getAction(entity, "Mulligan");
			assertTrue(action + " not allowed for " + entity, action.perform(entity));
		}
		
		assertEquals(1, mana.getFor(phase.getCurrentEntity()));
		nextPhase();
		
		assertEquals(1, mana.getFor(phase.getCurrentEntity()));
		nextPhase();
		useFail(opponent(), PhrancisGame.END_TURN_ACTION);
		
		assertEquals(2, mana.getFor(phase.getCurrentEntity()));
		Entity entity;
		
		entity = findCardInDeck(isCreature.and(manaCost(1)));
		useFail(entity, PhrancisGame.PLAY_ACTION);
		
		Entity enchantment = cardToHand(e -> scrapCost.getFor(e) == 1);
		useFail(enchantment, PhrancisGame.ENCHANT_ACTION);
		
		Entity attackerPlayer = phase.getCurrentEntity();
		Entity attacker = cardToHand(isCreature.and(manaCost(1)));
		useAction(attacker, PhrancisGame.PLAY_ACTION);
		assertEquals(1, phase.getCurrentEntity().get(field).size());
		assertResource(attacker, PhrancisResources.SICKNESS, 1);
		assertResource(attacker, PhrancisResources.ATTACK_AVAILABLE, 1);
		useFail(attacker, PhrancisGame.ATTACK_ACTION);
		
		nextPhase();
		Entity opponent = phase.getCurrentEntity();
		nextPhase();
		
		List<Entity> possibleTargets = findPossibleTargets(attacker, PhrancisGame.ATTACK_ACTION);
		assertEquals(1, possibleTargets.size());
		assertEquals(1, attackPoints.getFor(attacker));
		assertResource(attacker, PhrancisResources.SICKNESS, 0);
		assertResource(attacker, PhrancisResources.ATTACK_AVAILABLE, 1);
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, opponent);
		assertEquals(9, health.getFor(opponent));
		assertEquals(0, attackPoints.getFor(attacker));
		
		nextPhase();
		Entity defender = cardToHand(isCreature.and(manaCost(3)).and(isB0T).and(health(3)));
		useAction(defender, PhrancisGame.PLAY_ACTION);
		assertEquals(3, health.getFor(defender));
		
		// Test attack - kill attacker (1/1) gets killed by defender (3/3)
		nextPhase();
		assertResource(attacker, PhrancisResources.SICKNESS, 0);
		assertResource(attacker, PhrancisResources.ATTACK_AVAILABLE, 1);
		useActionWithFailedTarget(attacker, PhrancisGame.ATTACK_ACTION, opponent);
		assertResource(defender, PhrancisResources.HEALTH, 3);
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, defender);
		assertTrue(attacker.isRemoved());
		assertFalse(defender.isRemoved());
		assertResource(defender, PhrancisResources.HEALTH, 3);
		
		nextPhase();
		nextPhase();
		
		// Test scrap
		Entity scrap = cardToHand(isCreature.and(manaCost(1)));
		useAction(scrap, PhrancisGame.PLAY_ACTION);
		assertResource(attackerPlayer, PhrancisResources.SCRAP, 0);
		
		useFail(scrap, PhrancisGame.SCRAP_ACTION, opponent());
		
		useAction(scrap, PhrancisGame.SCRAP_ACTION);
		assertResource(attackerPlayer, PhrancisResources.SCRAP, 1);
		
		nextPhase();
		nextPhase();
		
		assertResource(attackerPlayer, PhrancisResources.MANA, 6);
		attacker = cardToHand(isCreature.and(manaCost(1)));
		useAction(attacker, PhrancisGame.PLAY_ACTION);
		
		attacker = cardToHand(isCreatureType("Bio").and(health(4)));
		useAction(attacker, PhrancisGame.PLAY_ACTION);
		
		enchantment = cardToHand(e -> scrapCost.getFor(e) == 1 && health.getFor(e) == 1);
		useActionWithFailedTarget(enchantment, PhrancisGame.ENCHANT_ACTION, attackerPlayer);
		assertResource(attacker, PhrancisResources.ATTACK, 4);
		assertResource(attacker, PhrancisResources.HEALTH, 4);
		List<Entity> targets = enchantment.get(actions).getAction(PhrancisGame.ENCHANT_ACTION).getTargetSets().get(0).findPossibleTargets();
		assertEquals(1, targets.size());
		useActionWithTarget(enchantment, PhrancisGame.ENCHANT_ACTION, attacker);
		assertResource(attacker, PhrancisResources.ATTACK, 4);
		assertResource(attacker, PhrancisResources.HEALTH, 5);
		
		nextPhase();
		nextPhase();
		
		assertFalse(defender.isRemoved());
		assertResource(defender, PhrancisResources.ATTACK, 3);
		assertResource(defender, PhrancisResources.HEALTH, 3);
		assertResource(attacker, PhrancisResources.ATTACK, 4);
		assertResource(opponent, PhrancisResources.HEALTH, 9);
		// Attacking with attack 4 on a creature with health 3. Opponent has 9 life now, will lose 1 life because of trample damage
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, defender);
		assertResource(opponent, PhrancisResources.HEALTH, 9);
		assertTrue(defender.isRemoved());
	}

	private Entity opponent() {
		List<Entity> list = game.getEntitiesWithComponent(PlayerComponent.class).stream()
			.filter(entity -> entity != phase.getCurrentEntity())
			.collect(Collectors.toList());
		assertEquals("Found more than one opponent", 1, list.size());
		return list.get(0);
	}

	private Predicate<Entity> health(int value) {
		return entity -> health.getFor(entity) == value;
	}

	private Predicate<Entity> isCreatureType(String creatureType) {
		return entity -> entity.hasComponent(CreatureTypeComponent.class) &&
				entity.getComponent(CreatureTypeComponent.class).getCreatureType().equals(creatureType);
	}

	private void assertResource(Entity entity, ECSResource resource, int expected) {
		ResourceRetriever retriever = ResourceRetriever.forResource(resource);
		ECSResourceData res = retriever.resFor(entity);
		assertEquals("Unexpected resource " + resource + " for " + entity, expected, res.get());
	}

	private void useActionWithFailedTarget(Entity source, String actionName, Entity target) {
		ECSAction action = getAction(source, actionName);
		Objects.requireNonNull(action, source + " does not have action " + actionName);
		assertTrue(action.isAllowed(phase.getCurrentEntity()));
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		assertFalse("Did not expect target to be allowed", targets.addTarget(target));
		assertFalse("Did not expect action to be performed", action.perform(phase.getCurrentEntity()));
	}

	private List<Entity> findPossibleTargets(Entity entity, String actionName) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed(phase.getCurrentEntity()));
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		return targets.findPossibleTargets();
	}

	private void useActionWithTarget(Entity entity, String actionName, Entity target) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed(phase.getCurrentEntity()));
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		assertTrue("Target could not be added: " + target + " to action " + action, targets.addTarget(target));
		assertTrue(action + " could not be performed", action.perform(phase.getCurrentEntity()));
	}

	private void useFail(Entity entity, String actionName) {
		useFail(entity, actionName, phase.getCurrentEntity());
	}

	private void useFail(Entity entity, String actionName, Entity performer) {
		ECSAction action = getAction(entity, actionName);
		assertFalse("Did not expect action " + actionName + " to be allowed on " + entity + " by " + performer, action.isAllowed(performer));
	}

	private Entity findCardInDeck(Predicate<Entity> condition) {
		return deck.get(phase.getCurrentEntity()).stream().filter(condition).findAny().get();
	}

	private Entity cardToHand(Predicate<Entity> condition) {
		Entity player = phase.getCurrentEntity();
		HandComponent myHand = hand.get(player);
		Optional<Entity> inHand = myHand.stream().filter(condition).findAny();
		if (!inHand.isPresent()) {
			int previousHand = myHand.size();
			Entity inDeck = findCardInDeck(condition);
			card.get(inDeck).moveToBottom(myHand);
			card.get(myHand.getTopCard()).moveToBottom(deck.get(player));
			assertEquals(previousHand, myHand.size());
			return inDeck;
		}
		
		return inHand.get();
	}

	private Predicate<Entity> manaCost(int i) {
		return entity -> manaCost.getFor(entity) == i;
	}

	private void nextPhase() {
		useAction(phase.getCurrentEntity(), PhrancisGame.END_TURN_ACTION);
		logger.info("Next phase, current entity is now " + phase.getCurrentEntity() + " phase is " + phase.getCurrentPhase());
	}

	private void useAction(Entity entity, String actionName) {
		ECSAction action = getAction(entity, actionName);
		assertTrue("Action " + actionName + " is not allowed for " + entity, action.isAllowed(phase.getCurrentEntity()));
		action.perform(phase.getCurrentEntity());
	}

	private ECSAction getAction(Entity entity, String actionName) {
		ActionComponent available = Objects.requireNonNull(actions.get(entity), "Entity does not have any action component");
		return available.getAction(actionName);
	}
	
}
