package com.cardshifter.modapi.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;

public class ZoneComponent extends Component {

	private final LinkedList<Entity> cards = new LinkedList<>();
	private final Entity owner;
	private final Map<Entity, Boolean> known = new HashMap<>();
	private final String name;
	private final Entity zoneEntity;
	
	private boolean publicKnown;
	
	public ZoneComponent(Entity owner, String name) {
		this.owner = Objects.requireNonNull(owner, "Zone Owner cannot be null");
		this.name = Objects.requireNonNull(name, "Zone Name cannot be null");
		this.zoneEntity = owner.getGame().newEntity();
	}
	
	public Entity getOwner() {
		return owner;
	}
	
	public Entity getTopCard() {
		return cards.getFirst();
	}
	
	public boolean isKnownTo(Entity player) {
		return known.getOrDefault(player, publicKnown);
	}

	public void addOnBottom(Entity entity) {
		if (entity.hasComponent(CardComponent.class)) {
			throw new IllegalArgumentException("Entity " + entity + " already has CardComponent");
		}
		CardComponent comp = new CardComponent(null);
		entity.addComponent(comp);
		comp.moveToBottom(this);
	}
	
	public void shuffle() {
		Collections.shuffle(cards, getRandom());
	}

	public void setGloballyKnown(boolean publicKnown) {
		this.publicKnown = publicKnown;
	}
	
	protected void setKnown(Entity owner, boolean known) {
		this.known.put(owner, known);
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}
	
	public Entity getComponentEntity() {
		return this.getEntity();
	}
	
	void cardMoveFrom(Entity card) {
		cards.remove(card);
	}

	void cardMoveAtTop(Entity card) {
		cards.addFirst(card);
	}

	void cardMoveAtBottom(Entity card) {
		cards.addLast(card);
	}

	public void forEach(Consumer<? super Entity> action) {
		this.cards.forEach(action);
	}

	public int size() {
		return cards.size();
	}

	@Override
	public String toString() {
		return "Zone '" + name + "' [size=" + size() + ", owner=" + owner
				+ ", known=" + known + ", publicKnown=" + publicKnown + "]";
	}
	
	public Stream<Entity> stream() {
		return cards.stream();
	}

	public List<Entity> getCards() {
		return new ArrayList<>(cards);
	}

	public String getName() {
		return name;
	}

	public int getZoneId() {
		return zoneEntity.getId();
	}
	
}
