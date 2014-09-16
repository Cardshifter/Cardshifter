package net.zomis.cardshifter.ecs.actions.attack;

import java.util.Objects;

import net.zomis.cardshifter.ecs.actions.TargetableCheckEvent;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;

public abstract class SpecificActionTargetSystem implements ECSSystem {

	private final String actionName;

	public SpecificActionTargetSystem(String actionName) {
		this.actionName = Objects.requireNonNull(actionName);
	}

	@Override
	public final void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(TargetableCheckEvent.class, this::targetableCheck);
	}
	
	private void targetableCheck(TargetableCheckEvent event) {
		if (event.getAction().getName().equals(actionName)) {
			this.checkTargetable(event);
		}
	}

	protected abstract void checkTargetable(TargetableCheckEvent event);

}
