package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Entity;

public class HandComponent extends ZoneComponent {

	public HandComponent(Entity owner) {
		super(owner, "Hand");
		this.setKnown(owner, true);
	}

}
