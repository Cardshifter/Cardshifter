
package com.cardshifter.core.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.core.mod.JavaMod;
import com.cardshifter.core.mod.Mod;
import com.cardshifter.core.mod.Mods;

/**
 *
 * @author Frank van Heeswijk
 */
public class JavaModTest {
	@Test
	public void testLoadMod() throws IOException {
		Path tempFile = Files.createTempFile("javamodtest-load", ".jar");
		Mod javaMod = Mods.open(tempFile);
		assertEquals(JavaMod.class, javaMod.getClass());
	}
}
