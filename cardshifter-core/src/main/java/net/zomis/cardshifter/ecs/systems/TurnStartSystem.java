package net.zomis.cardshifter.ecs.systems;

import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.phase.Phase;
import net.zomis.cardshifter.ecs.phase.PhaseChangeEvent;

public class TurnStartSystem implements ECSSystem {
	
	private final Consumer<Phase> onStart;

	public TurnStartSystem(Consumer<Phase> onStart) {
		this.onStart = onStart;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseChangeEvent.class, event -> onStart.accept(event.getNewPhase()));
	}

}
