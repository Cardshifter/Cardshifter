package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;

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
