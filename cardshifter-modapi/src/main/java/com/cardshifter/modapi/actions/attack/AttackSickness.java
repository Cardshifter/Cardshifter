package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

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
