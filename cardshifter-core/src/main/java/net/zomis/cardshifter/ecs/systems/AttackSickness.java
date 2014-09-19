package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;

public class AttackSickness extends SpecificActionSystem {

	private final ResourceRetriever resource;
	
	public AttackSickness(ECSResource resource) {
		super("Attack");
		this.resource = ResourceRetriever.forResource(resource);
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
