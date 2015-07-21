package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;

import java.util.Objects;

public class CardComponent extends Component {

	private ZoneComponent currentZone;
	
	public CardComponent(ZoneComponent zoneComponent) {
		this.currentZone = zoneComponent;
	}
	
	public Entity getOwner() {
        ZoneComponent zone = getCurrentZone();
//        Objects.requireNonNull(getCurrentZone(), "Card is not on any zone: " + Entity.debugInfo(getEntity()));
		return zone == null ? null : zone.getOwner();
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
		if (event.getSource() != null) {
			event.getSource().cardMoveFrom(card);
		}

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
	
    @Override
    public String toString() {
        return "CardOnZone " + currentZone;
    }
}
