package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Component;
import net.zomis.cardshifter.ecs.base.Entity;

public class CardComponent extends Component {

	private ZoneComponent currentZone;
	
	public CardComponent(ZoneComponent zoneComponent) {
		this.currentZone = zoneComponent;
	}
	
	public Entity getOwner() {
		return getCurrentZone().getOwner();
	}
	
	public ZoneComponent getCurrentZone() {
		return currentZone;
	}

	public void moveToTop(ZoneComponent target) {
		moveTo(target, true);
	}
	
	public void moveToBottom(ZoneComponent target) {
		moveTo(target, false);
	}
	
	private void moveTo(ZoneComponent target, boolean top) {
		Entity card = getEntity();
		ZoneChangeEvent event = new ZoneChangeEvent(currentZone, target, card);
		
		executeEvent(event);
		event.getSource().cardMoveFrom(card);
		
		ZoneComponent dest = event.getDestination();
		if (dest != null) {
			if (top) {
				dest.cardMoveAtTop(card);
			}
			else {
				dest.cardMoveAtBottom(card);
			}
		}
		this.currentZone = dest;
	}
	
}
