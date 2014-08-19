package com.cardshifter.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

public class Events {

	private final Globals globals = JsePlatform.standardGlobals();
	private final File scriptPaths;
	
	public Events(File directory) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("must be a directory: " + directory);
		}
		LuaJC.install(globals);
		this.scriptPaths = directory;
	}
	
	public Zone zoneMove(Card card, Zone source, Zone destination) {
		// TODO: Execute some event for when cards get moved, should perhaps also be possible to cancel it
		// To support things like: "Whenever a creature enters the battlefield, you gain 1 life"
		// for now this only returns the new destination zone.
		// in the future this should perhaps be changed to an `ZoneChangeEvent` with methods like `setDestination` and `setCancelled(true)`
		return destination;
	}
	
	/**
	 * Execute Lua code for setting up game
	 * 
	 * @param game Game to setup
	 */
	public void startGame(Game game) {
		File file = new File(scriptPaths, "start.lua");
		if (!file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("File not found or is a directory: " + file);
		}
		String fileName = file.getAbsolutePath();
		try {
			Files.readAllLines(file.toPath()).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		globals.loadfile(fileName).call();
		globals.set("gadsame", CoerceJavaToLua.coerce(game));
		globals.set("filename", LuaValue.valueOf(fileName));
		
//        globals.load(new StringReader(codeTextArea.getText()), "interopTest").call();
        LuaValue applyFunction = globals.get("startGame");
        Varargs applyFunctionResult = applyFunction.invoke(CoerceJavaToLua.coerce(game));
        System.out.println("Result: " + applyFunctionResult);
	}
	
}
