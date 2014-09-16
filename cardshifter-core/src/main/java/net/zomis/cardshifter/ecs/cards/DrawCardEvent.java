package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.IEvent;

public class DrawCardEvent implements IEvent {

	private final Entity cardToDraw;
	private final Entity owner;

	public DrawCardEvent(Entity cardToDraw, Entity owner) {
		this.cardToDraw = cardToDraw;
		this.owner = owner;
	}
	
	public Entity getCardToDraw() {
		return cardToDraw;
	}
	
	public Entity getOwner() {
		return owner;
	}

}
