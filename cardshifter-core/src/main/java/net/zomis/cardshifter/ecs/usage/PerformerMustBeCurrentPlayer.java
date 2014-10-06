package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.RetrieverSingleton;
import net.zomis.cardshifter.ecs.phase.PhaseController;

public class PerformerMustBeCurrentPlayer implements ECSSystem {

	@RetrieverSingleton
	private PhaseController phases;
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, this::actionPerformerIsCurrentPlayer);
	}
	
	private void actionPerformerIsCurrentPlayer(ActionAllowedCheckEvent event) {
		if (phases.getCurrentEntity() == null) {
			// If current player is null, avoid preventing *all* actions
			return;
		}
		if (event.getPerformer() != phases.getCurrentEntity()) {
			event.setAllowed(false);
		}
	}

}
