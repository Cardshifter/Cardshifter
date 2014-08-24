package com.cardshifter.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

public class Events {

	public static final String ACTION_USED = "actionUsed";
	
	private final Globals globals = JsePlatform.standardGlobals();
	private final Map<String, List<LuaFunction>> eventListeners = new ConcurrentHashMap<>();
	
	public Events(InputStream stream) {
		InputStreamReader reader = new InputStreamReader(stream);
		globals.load(reader, "mainScript").call();
		LuaJC.install(globals);
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Zone zoneMove(Card card, Zone source, Zone destination) {
		// TODO: Execute some event for when cards get moved, should perhaps also be possible to cancel it
		// To support things like: "Whenever a creature enters the battlefield, you gain 1 life"
		// for now this only returns the new destination zone.
		// in the future this should perhaps be changed to an `ZoneChangeEvent` with methods like `setDestination` and `setCancelled(true)`
		return destination;
	}
	
// TODO: Add events: game started, game ended, turn started, turn ended, card played, card attacked, card died, card invoked ability
	// TODO: game:on('played', function(subject, event) ... end)
	
	/**
	 * Execute Lua code for setting up game
	 * 
	 * @param game Game to setup
	 */
	public void startGame(Game game) {
		globals.set("gadsame", CoerceJavaToLua.coerce(game));
		
//        globals.load(new StringReader(codeTextArea.getText()), "interopTest").call();
        LuaValue applyFunction = globals.get("startGame");
        Varargs applyFunctionResult = applyFunction.invoke(CoerceJavaToLua.coerce(game));
        System.out.println("Result: " + applyFunctionResult);
	}
	
	public void callEvent(String eventName, LuaValue source, LuaValue table) {
		eventListeners.getOrDefault(eventName, Collections.emptyList()).forEach(func -> func.call(source, table));
	}
	
	public void registerListener(String eventName, LuaValue function) {
		function.checkfunction();
		eventListeners.putIfAbsent(eventName, Collections.synchronizedList(new ArrayList<>()));
		eventListeners.get(eventName).add((LuaFunction) function);
	}
	
}
