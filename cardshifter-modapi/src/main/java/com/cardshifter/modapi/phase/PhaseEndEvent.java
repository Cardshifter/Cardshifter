package com.cardshifter.modapi.phase;


import com.cardshifter.modapi.base.ECSGame;

public class PhaseEndEvent extends PhaseChangeEvent {

	public PhaseEndEvent(PhaseController controller, ECSGame game, Phase from) {
		super(controller, game, from, null);
	}

	
}
