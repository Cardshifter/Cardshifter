package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;

public class EntityCannotUseSystem extends SpecificActionSystem {

	private final Entity entity;

	public EntityCannotUseSystem(Entity entity, String action) {
		super(action);
		this.entity = entity;
	}

	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		if (event.getEntity() == entity) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
	}

}
