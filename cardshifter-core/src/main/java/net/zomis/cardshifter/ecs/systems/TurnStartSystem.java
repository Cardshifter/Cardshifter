package net.zomis.cardshifter.ecs.systems;

import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Phase;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.events.PhaseChangeEvent;

public class TurnStartSystem implements System {
	
	private final Consumer<Phase> onStart;

	public TurnStartSystem(Consumer<Phase> onStart) {
		this.onStart = onStart;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(PhaseChangeEvent.class, event -> onStart.accept(event.getNewPhase()));
	}

}
