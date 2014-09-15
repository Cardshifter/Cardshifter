package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;

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
