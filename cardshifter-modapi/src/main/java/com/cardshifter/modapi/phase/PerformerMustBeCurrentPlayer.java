package com.cardshifter.modapi.phase;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.RetrieverSingleton;

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
