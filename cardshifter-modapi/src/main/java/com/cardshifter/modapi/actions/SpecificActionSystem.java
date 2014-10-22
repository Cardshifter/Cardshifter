package com.cardshifter.modapi.actions;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;

public abstract class SpecificActionSystem implements ECSSystem {

	private final String actionName;

	public SpecificActionSystem(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public final void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, this::canAfford);
		game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, this::perform);
	}
	
	private void canAfford(ActionAllowedCheckEvent event) {
		if (!isInterestingAction(event.getAction())) {
			return;
		}
		this.isAllowed(event);
	}
	
	protected void isAllowed(ActionAllowedCheckEvent event) {
	}

	private boolean isInterestingAction(ECSAction action) {
		return actionName.equals(action.getName());
	}

	private void perform(ActionPerformEvent event) {
		if (!isInterestingAction(event.getAction())) {
			return;
		}
		this.onPerform(event);
	}

	protected abstract void onPerform(ActionPerformEvent event);

	@Override
	public String toString() {
		return getClass().getName() + " [actionName=" + actionName + "]";
	}
	
	public String getActionName() {
		return actionName;
	}
	
}
