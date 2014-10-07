package net.zomis.cardshifter.ecs.phase;


public class PhaseEndEvent extends PhaseChangeEvent {

	public PhaseEndEvent(PhaseController controller, Phase from) {
		super(controller, from, null);
	}

	
}
