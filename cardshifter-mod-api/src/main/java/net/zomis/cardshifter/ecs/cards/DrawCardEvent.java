package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.CancellableEvent;
import net.zomis.cardshifter.ecs.base.Entity;

public class DrawCardEvent implements CancellableEvent {

	private final Entity cardToDraw;
	private final Entity owner;
	private final ZoneComponent fromZone;
	private final ZoneComponent toZone;
	private boolean cancelled;

	public DrawCardEvent(Entity cardToDraw, Entity owner, ZoneComponent fromZone, ZoneComponent toZone) {
		this.cardToDraw = cardToDraw;
		this.owner = owner;
		this.fromZone = fromZone;
		this.toZone = toZone;
	}
	
	public Entity getCardToDraw() {
		return cardToDraw;
	}
	
	public Entity getOwner() {
		return owner;
	}
	
	public ZoneComponent getFromZone() {
		return fromZone;
	}
	
	public ZoneComponent getToZone() {
		return toZone;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
}
