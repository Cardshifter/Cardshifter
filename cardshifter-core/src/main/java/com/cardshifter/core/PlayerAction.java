package com.cardshifter.core;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class PlayerAction extends UsableAction {

	private final Player player;

	public PlayerAction(Player player, String name, LuaValue actionAllowed, LuaValue actionPerformed) {
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

}
