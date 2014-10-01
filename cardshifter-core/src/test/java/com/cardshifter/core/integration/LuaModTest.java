
package com.cardshifter.core.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.core.mod.LuaMod;
import com.cardshifter.core.mod.Mod;
import com.cardshifter.core.mod.Mods;

/**
 *
 * @author Frank van Heeswijk
 */
public class LuaModTest {
	@Test
	public void testLoadMod() throws IOException {
		Path tempFile = Files.createTempFile("luamodtest-load", ".zip");
		Mod luaMod = Mods.open(tempFile);
		assertEquals(LuaMod.class, luaMod.getClass());
	}
}
