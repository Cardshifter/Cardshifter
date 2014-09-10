
package com.cardshifter.core.issues;

import static org.junit.Assert.*;
import org.junit.Test;
import org.luaj.vm2.LuaError;

import com.cardshifter.core.Events;
import com.cardshifter.core.Game;

/**
 *
 * @author Frank van Heeswijk
 */
public class Issue16Test {
	@Test
	public void testLuaJavaNotAccessible() {
		try {
			Events events = new Events(Events.class.getResourceAsStream("issues/issue16/start.lua"));
			events.startGame(new Game(Game.class.getResourceAsStream("start.lua")));
		} catch (LuaError ex) {
			assertEquals("mainScript:3 attempt to call nil", ex.getMessage());
			return;
		}
		fail();
	}
	
	public static void throwException() throws Exception {
		throw new Exception();
	}
}
