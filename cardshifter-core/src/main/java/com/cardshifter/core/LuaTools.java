package com.cardshifter.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LuaTools {
	
	public static Map<String, String> tableToJava(LuaTable luaTable) {
		Map<String, String> result = new HashMap<>();
		processLuaTable(luaTable, (key, value) -> result.put(key.tojstring(), value.tojstring()));
		return result;
	}

	public static void processLuaTable(final LuaTable luaTable, final BiConsumer<LuaValue, LuaValue> pairConsumer) {
		luaTable.checktable();
		Objects.requireNonNull(pairConsumer, "pairConsumer");
		//search uses last key to find next key, starts with NIL
		LuaValue key = LuaValue.NIL;
		while (true) {
			Varargs pair = luaTable.next(key);
			key = pair.arg1();
			if (key.isnil()) {
				//no more keys
				break;
			}
			pairConsumer.accept(key, pair.arg(2));
		}
	}
	
}
