package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Entity;

public class BattlefieldComponent extends ZoneComponent {

	public BattlefieldComponent(Entity owner) {
		super(owner, "Battlefield");
		this.setGloballyKnown(true);
	}

}
