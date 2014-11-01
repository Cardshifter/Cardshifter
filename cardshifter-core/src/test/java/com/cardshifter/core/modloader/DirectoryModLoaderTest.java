
package com.cardshifter.core.modloader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Frank van Heeswijk
 */
public class DirectoryModLoaderTest {
	private final static String MOD_NAME = "cardshifter-mod-examples-java";
	
	@Test
	public void testLoad() throws URISyntaxException, ModNotLoadableException {
		Path testResourcesPath = Paths.get(getClass().getClassLoader().getResource("com/cardshifter/core/integration/").toURI());
		ModLoader modLoader = new DirectoryModLoader(testResourcesPath);
		
		modLoader.load(MOD_NAME);
	}

	@Test(expected = ClassNotFoundException.class)
	public void testUnload() throws ModNotLoadableException, URISyntaxException, ClassNotFoundException, IOException {
		Path testResourcesPath = Paths.get(getClass().getClassLoader().getResource("com/cardshifter/core/integration/").toURI());
		ModLoader modLoader = new DirectoryModLoader(testResourcesPath);
		
		Properties properties = ModLoaderHelper.getConfiguration(testResourcesPath.resolve(MOD_NAME));
		
		modLoader.load(MOD_NAME);
		modLoader.unload(MOD_NAME);
		Class.forName(properties.getProperty("entryPoint"));
	}

	@Test
	public void testGetLoadedMods() throws ModNotLoadableException, URISyntaxException {
		Path testResourcesPath = Paths.get(getClass().getClassLoader().getResource("com/cardshifter/core/integration/").toURI());
		ModLoader modLoader = new DirectoryModLoader(testResourcesPath);
		
		assertEquals(0, modLoader.getLoadedMods().size());
		
		Mod mod = modLoader.load(MOD_NAME);
		
		assertEquals(1, modLoader.getLoadedMods().size());
		assertTrue(modLoader.getLoadedMods().containsKey(MOD_NAME));
		
		assertEquals(mod, modLoader.getLoadedMods().get(MOD_NAME));
	}
	
	@Test
	public void testGetAvailableMods() throws URISyntaxException {
		Path testResourcesPath = Paths.get(getClass().getClassLoader().getResource("com/cardshifter/core/integration/").toURI());
		ModLoader modLoader = new DirectoryModLoader(testResourcesPath);
		
		assertEquals(Arrays.asList(MOD_NAME), modLoader.getAvailableMods());
	}
}