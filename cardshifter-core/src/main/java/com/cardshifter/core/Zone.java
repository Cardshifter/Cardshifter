package com.cardshifter.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2.LuaValue;

public class Zone {

	private final LinkedList<Card> cards; // `LinkedList` is both a `Deque` and a `List`
	private final Game game;
	private final Player owner;
	private final String name;
	private final Map<Player, Boolean> knownToPlayers = new ConcurrentHashMap<>();
	public final LuaValue data;
	private boolean globallyKnown;
        private Random random;
	
	Zone(Player owner, String name) {
		Objects.requireNonNull(owner);
		this.owner = owner;
		this.game = owner.getGame();
		this.cards = new LinkedList<>();
		this.data = LuaValue.tableOf();
		this.name = name;
                this.random = new Random();
	}
	
	public LinkedList<Card> getCards() {
		return cards;
	}
	
	public boolean isKnownToPlayer(Player player) {
		Objects.requireNonNull(player);
		return knownToPlayers.getOrDefault(player, this.globallyKnown);
	}
	
	public void setGloballyKnown(boolean globallyKnown) {
		this.globallyKnown = globallyKnown;
	}
	
	public void setKnown(Player player, boolean known) {
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
                int randomNumber = this.random.nextInt(150) + 1;
		Card card = new Card(this, randomNumber);
		this.cards.addFirst(card);
		return card;
	}
	
	public Card createCardOnBottom() {
                int randomNumber = this.random.nextInt(150) + 1;
		Card card = new Card(this, randomNumber);
		this.cards.addLast(card);
		return card;
	}
	
	public Card getTopCard() {
		return cards.getFirst();
	}
	
	public Card getBottomCard() {
		return cards.getLast();
	}
	
	@Override
	public String toString() {
		return "{Zone " + this.name + " (" + this.cards.size() + ") owned by " + this.owner + "}";
	}
	
	public void shuffle() {
		Collections.shuffle(this.cards, getGame().getRandom());
	}
	
	public boolean isEmpty() {
		return this.cards.isEmpty();
	}
}
