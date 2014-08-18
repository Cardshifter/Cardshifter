package com.cardshift.core;

import java.util.Objects;

import org.luaj.vm2.LuaValue;

public class Player {

	private final Game game;
	public final LuaValue data;
	
	public Player(Game game) {
		Objects.requireNonNull(game);
		this.game = game;
		this.data = LuaValue.tableOf();
	}
	
	public Game getGame() {
		return game;
	}
	
}
