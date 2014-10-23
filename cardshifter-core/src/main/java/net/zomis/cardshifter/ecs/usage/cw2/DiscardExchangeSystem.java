package net.zomis.cardshifter.ecs.usage.cw2;

import java.util.List;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.DrawStartCards;

public class DiscardExchangeSystem extends SpecificActionSystem {

	public DiscardExchangeSystem(String actionName) {
		super(actionName);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		List<Entity> targets = event.getAction().getTargetSets().get(0).getChosenTargets();
		for (Entity e : targets) {
			DrawStartCards.drawCard(e.getComponent(CardComponent.class).getOwner());
			
			e.destroy();
		}
		
	}


}
