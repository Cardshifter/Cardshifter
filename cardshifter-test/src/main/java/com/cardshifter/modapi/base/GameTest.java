package com.cardshifter.modapi.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.DeckComponent;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ResourceRetriever;

public abstract class GameTest {
	
	private static final Logger logger = LogManager.getLogger(GameTest.class);
	
	protected ECSGame game;
	
	@Retriever protected ComponentRetriever<ActionComponent> actions;
	@Retriever protected ComponentRetriever<CardComponent> card;
	@Retriever protected ComponentRetriever<DeckComponent> deck;
	@Retriever protected ComponentRetriever<HandComponent> hand;
	@Retriever protected ComponentRetriever<BattlefieldComponent> field;
	@Retriever protected ComponentRetriever<CreatureTypeComponent> ctype;
	@RetrieverSingleton protected PhaseController phase;
	
	@Before
	public void before() {
		game = new ECSGame();
		game.setRandomSeed(42);
		setupGame(game);
		Retrievers.inject(this, game);
		game.startGame();
		onAfterGameStart();
	}

	protected abstract void onAfterGameStart();
	protected abstract void setupGame(ECSGame ecsGame);

	protected Entity getOpponent() {
		List<Entity> list = game.getEntitiesWithComponent(PlayerComponent.class).stream()
			.filter(entity -> entity != phase.getCurrentEntity())
			.collect(Collectors.toList());
		assertEquals("Found more than one opponent", 1, list.size());
		return list.get(0);
	}

	protected Predicate<Entity> isCreatureType(String creatureType) {
		return entity -> entity.hasComponent(CreatureTypeComponent.class) &&
				entity.getComponent(CreatureTypeComponent.class).getCreatureType().equals(creatureType);
	}

	protected void assertResource(Entity entity, ECSResource resource, int expected) {
		ResourceRetriever retriever = ResourceRetriever.forResource(resource);
		ECSResourceData res = retriever.resFor(entity);
		assertEquals("Unexpected resource " + resource + " for " + entity, expected, res.get());
	}

	protected void useActionWithFailedTarget(Entity source, String actionName, Entity target) {
		ECSAction action = getAction(source, actionName);
		Objects.requireNonNull(action, source + " does not have action " + actionName);
		assertTrue(action.isAllowed(phase.getCurrentEntity()));
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		assertFalse("Did not expect target to be allowed", targets.addTarget(target));
		assertFalse("Did not expect action to be performed", action.perform(phase.getCurrentEntity()));
	}

	protected List<Entity> findPossibleTargets(Entity entity, String actionName) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed(phase.getCurrentEntity()));
		assertEquals(1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		return targets.findPossibleTargets();
	}

	protected void useActionWithTarget(Entity entity, String actionName, Entity target) {
		ECSAction action = getAction(entity, actionName);
		assertTrue(action.isAllowed(phase.getCurrentEntity()));
		assertEquals("Unexpected numbet of TargetSets.", 1, action.getTargetSets().size());
		TargetSet targets = action.getTargetSets().get(0);
		assertTrue("Target could not be added: " + target + " to action " + action, targets.addTarget(target));
		assertTrue(action + " could not be performed", action.perform(phase.getCurrentEntity()));
	}

	protected void useFail(Entity entity, String actionName) {
		useFail(entity, actionName, phase.getCurrentEntity());
	}

	protected void useFail(Entity entity, String actionName, Entity performer) {
		ECSAction action = getAction(entity, actionName);
		assertFalse("Did not expect action " + actionName + " to be allowed on " + entity + " by " + performer, action.isAllowed(performer));
	}

	protected Entity findCardInDeck(Predicate<Entity> condition) {
		return deck.get(phase.getCurrentEntity()).stream().filter(condition).findAny().get();
	}

	protected Entity cardToHand(Predicate<Entity> condition) {
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

	protected void nextPhase() {
		useAction(phase.getCurrentEntity(), "End Turn");
		logger.info("Next phase, current entity is now " + phase.getCurrentEntity() + " phase is " + phase.getCurrentPhase());
	}

	protected void useAction(Entity entity, String actionName) {
		ECSAction action = getAction(entity, actionName);
		assertTrue("Action " + actionName + " is not allowed for " + entity, action.isAllowed(phase.getCurrentEntity()));
		action.perform(phase.getCurrentEntity());
	}

	protected ECSAction getAction(Entity entity, String actionName) {
		if (actions == null) {
			throw new AssertionError("actions is null");
		}
		ActionComponent available = Objects.requireNonNull(actions.get(entity), "Entity does not have any action component");
		return available.getAction(actionName);
	}
	
	protected Entity currentPlayer() {
		return phase.getCurrentEntity();
	}

}
