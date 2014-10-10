
package com.cardshifter.core.integration;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.core.modloader.JavaMod;
import com.cardshifter.core.modloader.Mod;
import com.cardshifter.core.modloader.ModNotLoadableException;
import com.cardshifter.core.modloader.DirectoryModLoader;
import com.cardshifter.core.modloader.ModLoader;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ECSResourceMap;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Frank van Heeswijk
 */
public class JavaModTest {
	@Test
	public void testLoadMod() throws IOException, URISyntaxException, ModNotLoadableException {
		Path testResourcesPath = Paths.get(getClass().getClassLoader().getResource("com/cardshifter/core/integration/").toURI());
		ModLoader modLoader = new DirectoryModLoader(testResourcesPath);

		String modName = "cardshifter-mod-examples-java";
		Mod javaMod = modLoader.load(modName);
		
		assertEquals(JavaMod.class, javaMod.getClass());
		assertTrue(modLoader.getLoadedMods().containsKey(modName));
		assertEquals(javaMod, modLoader.getLoadedMods().get(modName));

		ECSGame game = javaMod.createGame();

		//assert two players
		Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
		assertEquals(2, players.size());

		for (Entity player : players) {
			ActionComponent actionComponent = player.getComponent(ActionComponent.class);

			//assert three actions
			assertEquals(3, actionComponent.getECSActions().size());

			//assert available resources
			ECSResourceMap resourceMap = player.getComponent(ECSResourceMap.class);
			List<ECSResourceData> resources = resourceMap.getResources().collect(Collectors.toList());
			ECSResourceData testResource = resources.get(0);
			assertEquals(10, testResource.get());
			//TODO check that this belongs to resource called "TEST"
		}
	}
}
