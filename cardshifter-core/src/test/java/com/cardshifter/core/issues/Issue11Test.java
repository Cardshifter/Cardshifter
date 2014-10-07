package com.cardshifter.core.issues;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.DeckComponent;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.LimitedHandSizeSystem;

public class Issue11Test {

	@Test
	public void handLimit() {
		ECSGame game = new ECSGame();
		Entity owner = game.newEntity();
		DeckComponent deck = new DeckComponent(owner);
		HandComponent hand = new HandComponent(owner);
		owner.addComponents(deck, hand);
		List<Entity> cards = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			Entity newCard = game.newEntity();
			deck.addOnBottom(newCard);
			cards.add(newCard);
		}
		
		List<Entity> denied = new ArrayList<>();
		game.addSystem(new LimitedHandSizeSystem(5, event -> denied.add(event.getCardToDraw())));
		game.startGame();
		
		DrawStartCards.drawCard(owner);
		DrawStartCards.drawCard(owner);
		DrawStartCards.drawCard(owner);
		DrawStartCards.drawCard(owner);
		DrawStartCards.drawCard(owner);
		assertTrue("A card has been denied prematurely", denied.isEmpty());
		DrawStartCards.drawCard(owner);
		assertEquals("Card was not denied", 1, denied.size());
		assertEquals("Unexpected card was denied", cards.get(5), denied.get(0));
		DrawStartCards.drawCard(owner);
		DrawStartCards.drawCard(owner);
		DrawStartCards.drawCard(owner);
		assertEquals("Cards has not been denied", 4, denied.size());
	}
}
