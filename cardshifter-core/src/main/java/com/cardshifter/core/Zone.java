package com.cardshifter.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2.LuaTable;

public class Zone implements IdEntity {
	public final LuaTable data = new ExtLuaTable(this::onChange);

	private final LinkedList<Card> cards = new LinkedList<>(); // `LinkedList` is both a `Deque` and a `List`
	private final Game game;
	private final Player owner;
	private final String name;
	private final Map<Player, Boolean> knownToPlayers = new ConcurrentHashMap<>();
	private boolean globallyKnown;
	private final int id;
	
	private void onChange(Object key, Object value) {
		System.out.println(this + ": " + key + " = " + value);
		getGame().broadcastChange(this, key, value);
	}
	
	Zone(final Player owner, final String name, final int id) {
		this.id = id;
		this.owner = Objects.requireNonNull(owner, "owner");
		this.game = owner.getGame();
		this.name = Objects.requireNonNull(name, "name");
	}
	
	public LinkedList<Card> getCards() {
		return cards;
	}
	
	public boolean isKnownToPlayer(final Player player) {
		Objects.requireNonNull(player, "player");
		return knownToPlayers.getOrDefault(player, this.globallyKnown);
	}
	
	public void setGloballyKnown(final boolean globallyKnown) {
		this.globallyKnown = globallyKnown;
	}
	
	public void setKnown(final Player player, final boolean known) {
		Objects.requireNonNull(player, "player");
		this.knownToPlayers.put(player, known);
	}
	
	public Game getGame() {
		return game;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public String getName() {
		return name;
	}
	
	public Card createCardOnTop() {
		Card card = new Card(this, getGame().nextId());
		this.cards.addFirst(card);
		return card;
	}
	
	public Card createCardOnBottom() {
		Card card = new Card(this, getGame().nextId());
		this.cards.addLast(card);
		return card;
	}
	
	public Card getTopCard() {
		return cards.getFirst();
	}
	
	public Card getBottomCard() {
		return cards.getLast();
	}
	
	public void shuffle() {
		Collections.shuffle(this.cards, getGame().getRandom());
	}
	
	public boolean isEmpty() {
		return this.cards.isEmpty();
	}
	
	@Override
	public int getId() {
		return id;
	}

	public int size() {
		return cards.size();
	}
	
	public String toString() {
		return "{Zone " + this.name + " (" + this.cards.size() + ") owned by " + this.owner + "}";
	}
}
