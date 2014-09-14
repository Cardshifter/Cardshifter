package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Entity;

public class DeckComponent extends ZoneComponent {

	public DeckComponent(Entity owner) {
		super(owner, "Deck");
	}

}
