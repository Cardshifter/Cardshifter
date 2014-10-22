package com.cardshifter.modapi.actions.attack;

import java.util.Objects;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;

public abstract class SpecificActionTargetSystem implements ECSSystem {

	private final String actionName;

	public SpecificActionTargetSystem(String actionName) {
		this.actionName = Objects.requireNonNull(actionName);
	}

	@Override
	public final void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, TargetableCheckEvent.class, this::targetableCheck);
	}
	
	private void targetableCheck(TargetableCheckEvent event) {
		if (event.getAction().getName().equals(actionName)) {
			this.checkTargetable(event);
		}
	}

	protected abstract void checkTargetable(TargetableCheckEvent event);

}
