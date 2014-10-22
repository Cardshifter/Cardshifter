
package com.cardshifter.core.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.core.modloader.LuaMod;
import com.cardshifter.core.modloader.Mod;
import com.cardshifter.core.modloader.ModNotLoadableException;
import com.cardshifter.core.modloader.DirectoryModLoader;
import org.junit.Ignore;

/**
 *
 * @author Frank van Heeswijk
 */
@Ignore
public class LuaModTest {
	@Test
	public void testLoadMod() throws IOException, ModNotLoadableException {
//		Path tempFile = Files.createTempFile("luamodtest-load", ".zip");
//		Mod luaMod = DirectoryModLoader.load(tempFile, "lua");
//		assertEquals(LuaMod.class, luaMod.getClass());
//		Files.delete(tempFile);
	}
}
