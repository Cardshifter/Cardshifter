package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceRetreiver;

public class AttackSickness extends SpecificActionSystem {

	private final ResourceRetreiver resource;
	
	public AttackSickness(ECSResource resource) {
		super("Attack");
		this.resource = ResourceRetreiver.forResource(resource);
	}

	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		if (resource.getFor(event.getEntity()) > 0) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
	}

}
