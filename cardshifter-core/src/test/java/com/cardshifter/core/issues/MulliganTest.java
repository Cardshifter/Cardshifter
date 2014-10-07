package com.cardshifter.core.issues;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.DeckComponent;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.MulliganSingleCards;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceDefault;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class MulliganTest {

	private final ECSGame game = new ECSGame();
	private final ECSResource standard = new ECSResourceDefault("Test");
	private final ResourceRetriever standardRetr = ResourceRetriever.forResource(standard);
	private final ComponentRetriever<HandComponent> hand = ComponentRetriever.retreiverFor(HandComponent.class);
	
	
	@Test
	public void handLimit() {
		Entity player0 = createPlayer(0);
		Entity player1 = createPlayer(1);
		game.newEntity().addComponent(new PhaseController().addPhase(new Phase(game.newEntity(), "Main")));
		
		game.addSystem(new DrawStartCards(4));
		game.addSystem(new MulliganSingleCards(game));
		game.startGame();
		
		final PhaseController phases = ComponentRetriever.singleton(game, PhaseController.class);
		
		assertEquals("Mulligan", phases.getCurrentPhase().getName());
		
		assertFalse(actionFor(player0).isAllowed(player1));
		assertFalse(actionFor(player1).isAllowed(player0));
		
		assertHandCards(player0, new int[]{ 0, 1, 2, 3 });
		assertHandCards(player1, new int[]{ 0, 1, 2, 3 });
		
		performMulligan(player0, new int[]{ 0, 2, 3 });
		assertNull(actionFor(player0));
		performMulligan(player1, new int[]{ 1 });
		assertNull(actionFor(player1));
		
		assertHandCards(player0, new int[]{ 1, 4, 5, 6 });
		assertHandCards(player1, new int[]{ 0, 2, 3, 4 });

		assertEquals("Main", phases.getCurrentPhase().getName());
//		assertFalse(player0 mulligan allowed for player1);
		
	}

	private ECSAction actionFor(Entity player0) {
		return player0.getComponent(ActionComponent.class).getAction("Mulligan");
	}

	private void performMulligan(Entity player, int[] cardsToChange) {
		HandComponent zone = player.get(hand);
		ECSAction action = player.getComponent(ActionComponent.class).getAction("Mulligan");
		assertNotNull("Action not found on " + player);
		TargetSet targets = action.getTargetSets().get(0);
		Stream<Entity> chosenTargets = Arrays.stream(cardsToChange).mapToObj(i -> zone.stream().filter(e -> standardRetr.getFor(e) == i).findAny().get());
		chosenTargets.forEach(e -> targets.addTarget(e));
		assertTrue("Mulligan was not performed", action.perform(player));
	}

	private void assertHandCards(Entity player, int[] expectedCards) {
		HandComponent zone = player.get(hand);
		int[] cards = zone.getCards().stream().mapToInt(e -> standardRetr.getFor(e)).toArray();
		assertArrayEquals(expectedCards, cards);
		
	}

	private Entity createPlayer(int index) {
		Entity owner = game.newEntity();
		owner.addComponent(new PlayerComponent(index, "Player " + index));
		DeckComponent deck = new DeckComponent(owner);
		HandComponent hand = new HandComponent(owner);
		owner.addComponents(deck, hand);
		List<Entity> cards = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			Entity newCard = game.newEntity();
			ECSResourceMap.createFor(newCard).set(standard, i);
			deck.addOnBottom(newCard);
			cards.add(newCard);
		}
		return owner;
	}
}
