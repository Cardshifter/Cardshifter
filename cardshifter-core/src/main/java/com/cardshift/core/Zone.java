package com.cardshift.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.luaj.vm2.LuaValue;

public class Zone {

	private final List<Card> cards;
	private final Game game;
	private final Player owner;
	private final LuaValue luaData;
	
	public Zone(Player owner) {
		Objects.requireNonNull(owner);
		this.owner = owner;
		this.game = owner.getGame();
		this.cards = new ArrayList<>();
		this.luaData = LuaValue.tableOf();
	}
	
	public List<Card> getCards() {
		return cards;
	}
	
	public Game getGame() {
		return game;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public LuaValue getLuaData() {
		return luaData;
	}
	
}
