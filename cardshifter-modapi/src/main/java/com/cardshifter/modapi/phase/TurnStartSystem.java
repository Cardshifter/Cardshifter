package com.cardshifter.modapi.phase;

import java.util.function.Consumer;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;

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
