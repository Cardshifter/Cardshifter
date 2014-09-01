package com.cardshifter.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

public class Events {
	//TODO refactor to enums
	public static final String ACTION_USED = "actionUsed";

	public static final String TURN_END = "turnEnd";
	public static final String TURN_START = "turnStart";
	
	private final Globals globals = JsePlatform.standardGlobals();
	private final Map<String, List<LuaFunction>> eventListeners = new ConcurrentHashMap<>();
	
	public Events(final InputStream inputStream) {
		Objects.requireNonNull(inputStream, "inputStream");
		InputStreamReader reader = new InputStreamReader(inputStream);
		globals.load(reader, "mainScript").call();
		LuaJC.install(globals);
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();	//TODO logging?
		}
	}
	
	public Zone zoneMove(final Card card, final Zone source, final Zone destination) {
		// TODO: Execute some event for when cards get moved, should perhaps also be possible to cancel it
		// To support things like: "Whenever a creature enters the battlefield, you gain 1 life"
		// for now this only returns the new destination zone.
		// in the future this should perhaps be changed to an `ZoneChangeEvent` with methods like `setDestination` and `setCancelled(true)`
		return destination;
	}
	
	// TODO: Add events: game started, game ended, card attacked, card died, card invoked ability
	
	/**
	 * Execute Lua code for setting up game
	 * 
	 * @param game Game to setup
	 */
	public void startGame(final Game game) {
		globals.set("gadsame", CoerceJavaToLua.coerce(game));
		
//        globals.load(new StringReader(codeTextArea.getText()), "interopTest").call();
        LuaValue applyFunction = globals.get("startGame");
        Varargs applyFunctionResult = applyFunction.invoke(CoerceJavaToLua.coerce(game));
        System.out.println("Result: " + applyFunctionResult);
        game.setCurrentPlayer(game.getFirstPlayer());
	}
	
	public void callEvent(final String eventName, final LuaValue source, final LuaValue table) {
		Objects.requireNonNull(eventName, "eventName");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(table, "table");
		eventListeners.getOrDefault(eventName, Collections.emptyList())
			.forEach(func -> func.call(source, table));
	}
	
	public void registerListener(final String eventName, final LuaValue function) {
		eventListeners.putIfAbsent(eventName, Collections.synchronizedList(new ArrayList<>()));
		eventListeners.get(eventName).add(function.checkfunction());
	}
}
