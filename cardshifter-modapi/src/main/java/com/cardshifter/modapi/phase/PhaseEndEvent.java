package com.cardshifter.modapi.phase;


public class PhaseEndEvent extends PhaseChangeEvent {

	public PhaseEndEvent(PhaseController controller, Phase from) {
		super(controller, from, null);
	}

	
}
