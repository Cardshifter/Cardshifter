package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.Entity;

public class BattlefieldComponent extends ZoneComponent {

	public BattlefieldComponent(Entity owner) {
		super(owner, "Battlefield");
		this.setGloballyKnown(true);
	}

}
