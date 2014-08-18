package com.cardshift.core;

import org.luaj.vm2.LuaValue;

public class Card {

	private final LuaValue luaData;
	
	public Card() {
		luaData = LuaValue.tableOf();
	}
	
	public LuaValue getLuaData() {
		return luaData;
	}
	
}
