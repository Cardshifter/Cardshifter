package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.zomis.cardshifter.ecs.usage.ConfigComponent;
import net.zomis.cardshifter.ecs.usage.DeckConfig;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class PhrancisTest extends GameTest {

	private static final int originalLife = 30;
	private PhaseController phase;
	private final ResourceRetriever mana = ResourceRetriever.forResource(PhrancisResources.MANA);
	private final ResourceRetriever manaCost = ResourceRetriever.forResource(PhrancisResources.MANA_COST);
	private final ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
	private final ResourceRetriever attackPoints = ResourceRetriever.forResource(PhrancisResources.ATTACK_AVAILABLE);
	private final ResourceRetriever scrapCost = ResourceRetriever.forResource(PhrancisResources.SCRAP_COST);
	
	private final ComponentRetriever<BattlefieldComponent> field = ComponentRetriever.retreiverFor(BattlefieldComponent.class);
	
	private final Predicate<Entity> isB0T = isCreatureType("B0T");
	private final Predicate<Entity> isCreature = entity -> entity.hasComponent(CreatureTypeComponent.class);
			
	@Override
	protected void setupGame(ECSGame game) {
		PropertyConfigurator.configure(PhrancisTest.class.getResourceAsStream("log4j.properties"));
		ECSMod mod = new PhrancisGame();
		mod.declareConfiguration(game);
		
		// Configure decks
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
	public void mulligan() {
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
	}
	
	@Test
	@Ignore
	public void integration() {
		mulligan();
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
		assertEquals(originalLife - 1, health.getFor(opponent));
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
		List<Entity> targets = getAction(enchantment, PhrancisGame.ENCHANT_ACTION).getTargetSets().get(0).findPossibleTargets();
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

	private Predicate<Entity> manaCost(int i) {
		return entity -> manaCost.getFor(entity) == i;
	}

	@Override
	protected void onBefore() {
	}

}
