package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.phase.PhaseStartEvent;

public class DrawCardAtBeginningOfTurnSystem implements ECSSystem {

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, this::drawCard);
	}
	
	private void drawCard(PhaseStartEvent event) {
		DrawStartCards.drawCard(event.getNewPhase().getOwner());
	}

}
