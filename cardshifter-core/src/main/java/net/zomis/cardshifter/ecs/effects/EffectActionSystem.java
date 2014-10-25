package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Retriever;

public class EffectActionSystem extends SpecificActionSystem {

	@Retriever
	private ComponentRetriever<EffectComponent> effect;
	
	public EffectActionSystem(String actionName) {
		super(actionName);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		if (effect.has(event.getEntity())) {
			EffectComponent eff = effect.get(event.getEntity());
			eff.perform(event);
		}
	}


}
