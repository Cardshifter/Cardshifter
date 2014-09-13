package net.zomis.cardshifter.ecs.events;

import net.zomis.cardshifter.ecs.base.Phase;
import net.zomis.cardshifter.ecs.components.PhaseController;

public class PhaseStartEvent extends PhaseChangeEvent {

	public PhaseStartEvent(PhaseController controller, Phase from, Phase to) {
		super(controller, from, to);
	}

}
