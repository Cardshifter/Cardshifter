package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;

public class DestroyAfterUseSystem extends SpecificActionSystem {

	public DestroyAfterUseSystem(String actionName) {
		super(actionName);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		event.getEntity().destroy();
	}

}
