package com.cardshifter.core;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class PlayerAction extends UsableAction {
	private final Player player;

	public PlayerAction(final Player player, final String name, final LuaValue actionAllowed, final LuaValue actionPerformed) {
		super(name, actionAllowed, actionPerformed);
		this.player = player;
	}

	@Override
	protected LuaValue methodArg() {
		return CoerceJavaToLua.coerce(player);
	}

	@Override
	protected Game getGame() {
		return player.getGame();
	}

	@Override
	public String toString() {
		return "{Action " + getName() + " for player " + player + "}";
	}
}
