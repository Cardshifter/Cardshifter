package com.cardshifter.server.utils.fight;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.events.EntityRemoveEvent;
import com.cardshifter.modapi.events.GameOverEvent;
import com.cardshifter.modapi.events.StartGameEvent;

public class PhrancisFight<A> {

	public PhrancisFight() {
		
	}
	
	EntityRemoveEvent whoDied;
	StartGameEvent cardsOnHand; // mana cost, num Bios, num enchantments
	ActionPerformEvent checkBiggestMinion, enchantmentPlayed;
	GameOverEvent healthOfWinning, healthOfLosing, attackInPlay, decksLeftInDeck;
	
//	@Register
	public void registerListeners(A obj) {
		// registerEventListeners and other hooks
		// events.registerHandlerAfter(GameOverEvent.class, fight.notify(42));
	}
//	DeckConfig whichDecksAreFighting
//	CardshifterAI whichAIsAreFighting
	
//	@Data()
	public void method() {
		
	}
	
	
}
