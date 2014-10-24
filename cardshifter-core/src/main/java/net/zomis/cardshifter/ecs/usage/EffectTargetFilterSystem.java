package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.actions.attack.SpecificActionTargetSystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Retriever;

public class EffectTargetFilterSystem extends SpecificActionTargetSystem {

	@Retriever
	private ComponentRetriever<FilterComponent> filter;
	
	public EffectTargetFilterSystem(String actionName) {
		super(actionName);
	}

	@Override
	protected void checkTargetable(TargetableCheckEvent event) {
		if (filter.has(event.getAction().getOwner())) {
			FilterComponent comp = filter.get(event.getAction().getOwner());
			event.setAllowed(comp.check(event));
		}
	}

}
