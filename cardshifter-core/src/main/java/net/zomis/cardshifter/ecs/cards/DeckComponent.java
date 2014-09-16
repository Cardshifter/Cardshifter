package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.Entity;

public class DeckComponent extends ZoneComponent {

	public DeckComponent(Entity owner) {
		super(owner, "Deck");
	}

}
