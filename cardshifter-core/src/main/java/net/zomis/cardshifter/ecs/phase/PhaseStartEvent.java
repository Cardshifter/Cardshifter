package net.zomis.cardshifter.ecs.phase;


public class PhaseStartEvent extends PhaseChangeEvent {

	public PhaseStartEvent(PhaseController controller, Phase from, Phase to) {
		super(controller, from, to);
	}

}
