package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.events.PhaseStartEvent;

public class DrawCardAtBeginningOfTurnSystem implements System {

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(PhaseStartEvent.class, this::drawCard);
	}
	
	private void drawCard(PhaseStartEvent event) {
		DrawStartCards.drawCard(event.getNewPhase().getOwner());
	}

}
