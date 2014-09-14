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
	
}
