package com.cardshifter.modapi.phase;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;

/**
 * <p>Functionality for automatically ending phase once x cards has been played that turn. (ActionZone cards not included)</p>
 * <p>Listens for {@link CardPlayedEvent} and {@link PhaseChangeEvent}</p>
 */
public class LimitedActionsPerTurnSystem implements ECSSystem {

	private int cardsPlayedThisTurn;
	private final int limit;
	private final String actionName;
	
	public LimitedActionsPerTurnSystem(int limit, String actionName) {
		this.limit = limit;
		this.actionName = actionName;
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, this::onNewTurn);
		game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, this::onCardPlayed);
	}
	
	private void onNewTurn(PhaseStartEvent event) {
		// TODO: This is technically not turn-dependent, only phase-dependent. One *turn* can consist of many *phases*
		this.cardsPlayedThisTurn = 0;
	}
	
	private void onCardPlayed(ActionPerformEvent event) {
		if (event.getAction().getName().equals(actionName)) {
			return;
		}
		this.cardsPlayedThisTurn++;
		if (this.cardsPlayedThisTurn >= limit) {
			ECSAction action = Actions.getAction(event.getPerformer(), actionName);
			action.perform(event.getPerformer());
			cardsPlayedThisTurn = 0;
		}
	}

}
