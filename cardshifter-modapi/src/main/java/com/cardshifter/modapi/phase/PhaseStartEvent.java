package com.cardshifter.modapi.phase;


import com.cardshifter.modapi.base.ECSGame;

public class PhaseStartEvent extends PhaseChangeEvent {

	public PhaseStartEvent(PhaseController controller, ECSGame game, Phase from, Phase to) {
		super(controller, game, from, to);
	}

}
