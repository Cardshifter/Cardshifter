
package com.cardshifter.core.integration;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.core.mod.JavaMod;
import com.cardshifter.core.mod.Mod;
import com.cardshifter.core.mod.ModNotLoadableException;
import com.cardshifter.core.mod.Mods;
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
        Path javaModFile = Paths.get(JavaModTest.class.getResource("cardshifter-mod-examples-java-0.1.jar").toURI());
		Mod javaMod = Mods.load(javaModFile, "java");
		assertEquals(JavaMod.class, javaMod.getClass());
        
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
