package com.cardshifter.core.actions;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.cardshifter.core.Game;
import com.cardshifter.core.Player;

public class PlayerAction extends UsableAction {
	private final Player player;

	public PlayerAction(final Player player, final String name, final LuaValue isAllowedFunction, final LuaValue performFunction) {
		super(name, isAllowedFunction, performFunction);
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

	@Override
	public int getEntityId() {
		return player.getId();
	}
	
}
