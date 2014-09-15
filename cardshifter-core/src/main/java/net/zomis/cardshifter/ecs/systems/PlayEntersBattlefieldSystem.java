package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.components.BattlefieldComponent;
import net.zomis.cardshifter.ecs.components.CardComponent;
import net.zomis.cardshifter.ecs.events.ActionPerformEvent;

public class PlayEntersBattlefieldSystem extends SpecificActionSystem {

	public PlayEntersBattlefieldSystem(String action) {
		super(action);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		CardComponent card = event.getEntity().getComponent(CardComponent.class);
		card.moveToBottom(card.getOwner().getComponent(BattlefieldComponent.class));
	}

}
