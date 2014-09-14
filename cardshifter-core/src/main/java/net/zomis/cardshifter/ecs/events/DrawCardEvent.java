package net.zomis.cardshifter.ecs.events;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class DrawCardEvent implements IEvent {

	private final Entity cardToDraw;

	public DrawCardEvent(Entity cardToDraw) {
		this.cardToDraw = cardToDraw;
	}
	
	public Entity getCardToDraw() {
		return cardToDraw;
	}

}
