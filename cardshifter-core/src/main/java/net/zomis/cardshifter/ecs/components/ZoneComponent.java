package net.zomis.cardshifter.ecs.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.base.Component;
import net.zomis.cardshifter.ecs.base.Entity;

public class ZoneComponent extends Component {

	private final LinkedList<Entity> cards = new LinkedList<>();
	private final Entity owner;
	private final Map<Entity, Boolean> known = new HashMap<>();
	private final String name;
	
	private boolean publicKnown;
	
	public ZoneComponent(Entity owner, String name) {
		this.owner = owner;
		this.name = name;
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
		entity.addComponent(new CardComponent(this));
		cardMoveAtBottom(entity);
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
	
}
