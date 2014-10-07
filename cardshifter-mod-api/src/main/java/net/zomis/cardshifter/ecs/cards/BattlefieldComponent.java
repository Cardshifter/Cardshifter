package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.Entity;

public class BattlefieldComponent extends ZoneComponent {

	public BattlefieldComponent(Entity owner) {
		super(owner, "Battlefield");
		this.setGloballyKnown(true);
	}

}
