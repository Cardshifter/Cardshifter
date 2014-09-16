package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.actions.TargetSet;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.DeckComponent;
import net.zomis.cardshifter.ecs.cards.HandComponent;
import net.zomis.cardshifter.ecs.components.CreatureTypeComponent;
import net.zomis.cardshifter.ecs.phase.PhaseController;
import net.zomis.cardshifter.ecs.resources.ResourceRetreiver;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import org.junit.Before;
import org.junit.Test;

public class PhrancisTest {

	private ECSGame game;
	private PhaseController phase;
	private final ResourceRetreiver mana = ResourceRetreiver.forResource(PhrancisResources.MANA);
	private final ResourceRetreiver manaCost = ResourceRetreiver.forResource(PhrancisResources.MANA_COST);
	private final ResourceRetreiver health = ResourceRetreiver.forResource(PhrancisResources.HEALTH);
	private final ResourceRetreiver attackPoints = ResourceRetreiver.forResource(PhrancisResources.ATTACK_AVAILABLE);
	
	private final ComponentRetriever<ActionComponent> actions = ComponentRetriever.retreiverFor(ActionComponent.class);
	private final ComponentRetriever<DeckComponent> deck = ComponentRetriever.retreiverFor(DeckComponent.class);
	private final ComponentRetriever<HandComponent> hand = ComponentRetriever.retreiverFor(HandComponent.class);
	private final ComponentRetriever<BattlefieldComponent> field = ComponentRetriever.retreiverFor(BattlefieldComponent.class);
	
	private final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
			
	@Before
	public void before() {
		game = PhrancisGame.createGame();
		game.startGame();
		phase = ComponentRetriever.singleton(game, PhaseController.class);
	}
	
	@Test
	public void integration() {
		assertEquals(1, mana.getFor(phase.getCurrentEntity()));
		nextPhase();
		assertEquals(1, mana.getFor(phase.getCurrentEntity()));
		nextPhase();
		
		assertEquals(2, mana.getFor(phase.getCurrentEntity()));
		Predicate<Entity> isCreature = entity -> entity.hasComponent(CreatureTypeComponent.class);
		Entity entity;
		
		entity = findCardInDeck(isCreature.and(manaCost(1)));
		useFail(entity, PhrancisGame.PLAY_ACTION);
		
		Entity attacker = cardToHand(isCreature.and(manaCost(1)));
		useAction(attacker, PhrancisGame.PLAY_ACTION);
		assertEquals(1, phase.getCurrentEntity().get(field).size());
		
		nextPhase();
		Entity opponent = phase.getCurrentEntity();
		nextPhase();
		
		List<Entity> possibleTargets = findPossibleTargets(attacker, PhrancisGame.ATTACK_ACTION);
		assertEquals(1, possibleTargets.size());
		assertEquals(1, attackPoints.getFor(attacker));
		useActionWithTarget(attacker, PhrancisGame.ATTACK_ACTION, opponent);
		assertEquals(9, health.getFor(opponent));
		assertEquals(0, attackPoints.getFor(attacker));
	}

	private List<Entity> findPossibleTargets(Entity entity, String actionName) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed());
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		return targets.findPossibleTargets();
	}

	private void useActionWithTarget(Entity entity, String actionName, Entity target) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed());
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		assertTrue(targets.addTarget(target));
		action.perform();
	}

	private void useFail(Entity entity, String actionName) {
		assertFalse(getAction(entity, actionName).isAllowed());
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
	}

	private void useAction(Entity entity, String actionName) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed());
		action.perform();
	}

	private ECSAction getAction(Entity entity, String actionName) {
		ActionComponent available = actions.get(entity);
		return available.getAction(actionName);
	}
	
}
