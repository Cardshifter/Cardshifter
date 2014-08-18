package com.cardshift.core;

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
