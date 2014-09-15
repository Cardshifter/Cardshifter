package net.zomis.cardshifter.ecs.actions;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;

public abstract class SpecificActionSystem implements ECSSystem {

	private final String actionName;

	public SpecificActionSystem(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public final void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(ActionAllowedCheckEvent.class, this::canAfford);
		game.getEvents().registerHandlerAfter(ActionPerformEvent.class, this::perform);
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
	
}
