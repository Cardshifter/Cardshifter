package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.phase.PhaseStartEvent;

public class DrawCardAtBeginningOfTurnSystem implements ECSSystem {

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, this::drawCard);
	}
	
	private void drawCard(PhaseStartEvent event) {
		if (event.getOldPhase().getOwner() == null) {
			return;
		}
		DrawStartCards.drawCard(event.getNewPhase().getOwner());
	}

}
