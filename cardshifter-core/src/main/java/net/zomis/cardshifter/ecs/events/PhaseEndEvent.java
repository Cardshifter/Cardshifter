package net.zomis.cardshifter.ecs.events;

import net.zomis.cardshifter.ecs.base.Phase;
import net.zomis.cardshifter.ecs.components.PhaseController;

public class PhaseEndEvent extends PhaseChangeEvent {

	public PhaseEndEvent(PhaseController controller, Phase from) {
		super(controller, from, null);
	}

	
}
