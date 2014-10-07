package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.Entity;

public class HandComponent extends ZoneComponent {

	public HandComponent(Entity owner) {
		super(owner, "Hand");
		this.setKnown(owner, true);
	}

}
