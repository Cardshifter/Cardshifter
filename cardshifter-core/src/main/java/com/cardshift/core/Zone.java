package com.cardshift.core;

import java.util.LinkedList;
import java.util.Objects;

import org.luaj.vm2.LuaValue;

public class Zone {

	private final LinkedList<Card> cards; // `LinkedList` is both a `Deque` and a `List`
	private final Game game;
	private final Player owner;
	private final String name;
	public final LuaValue data;
	
	Zone(Player owner, String name) {
		Objects.requireNonNull(owner);
		this.owner = owner;
		this.game = owner.getGame();
		this.cards = new LinkedList<>();
		this.data = LuaValue.tableOf();
		this.name = name;
	}
	
	public LinkedList<Card> getCards() {
		return cards;
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
		Card card = new Card(this);
		this.cards.addFirst(card);
		return card;
	}
	
	public Card createCardOnBottom() {
		Card card = new Card(this);
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
		return "{Zone " + this.name + " owned by " + this.owner + "}";
	}
	
}
