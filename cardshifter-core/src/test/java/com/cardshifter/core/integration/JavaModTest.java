
package com.cardshifter.core.integration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

/**
 *
 * @author Frank van Heeswijk
 */
public class JavaModTest {
	private final static String MOD_NAME = "cardshifter-mod-examples-java";
	
	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	@Test
	public void testLoadMod() throws IOException, URISyntaxException, ModNotLoadableException {
		Path testResourcesPath = Paths.get(getClass().getClassLoader().getResource("com/cardshifter/core/integration/").toURI());
		ModLoader modLoader = new DirectoryModLoader(testResourcesPath);

		Mod javaMod = modLoader.load(MOD_NAME);
		
		assertEquals(JavaMod.class, javaMod.getClass());
		assertTrue(modLoader.getLoadedMods().containsKey(MOD_NAME));
		assertEquals(javaMod, modLoader.getLoadedMods().get(MOD_NAME));

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
	
	@Test
	public void testLoadModSimple() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ModNotLoadableException {
		Path modLoaderDirectory = Files.createTempDirectory("modloader");
		modLoaderDirectory.toFile().deleteOnExit();
		
		Path modDirectory = Files.createTempDirectory(modLoaderDirectory, "simplemod");
		Path compileDirectory = Files.createTempDirectory("compileDirectory");
		
		String simpleModSourceString = RuntimeJarHelper.createModSourceString(
			"SimpleMod", 
			"import com.cardshifter.modapi.base.PlayerComponent;\n", 
			"game.newEntity().addComponent(new PlayerComponent(0, \"Test Player\"));\n"
		);
		
		Path simpleModSource = compileDirectory.resolve("SimpleMod.java");
		Files.createFile(simpleModSource);
		Files.write(simpleModSource, simpleModSourceString.getBytes(StandardCharsets.UTF_8));
		
		List<Path> compiledModSources = RuntimeJarHelper.compileJavaSource(simpleModSource, compileDirectory);
		
		Path jarFile = modDirectory.resolve("simplemod");
		Files.createFile(jarFile);
		RuntimeJarHelper.createJar(jarFile, compiledModSources);
		
		Properties properties = new Properties();
		properties.setProperty("language", "java");
		properties.setProperty("jar", jarFile.getFileName().toString());
		properties.setProperty("entryPoint", "com.cardshifter.core.integration.throwaway.runtimemod.SimpleMod");
		RuntimeJarHelper.createProperties(modDirectory, properties);
		
		ModLoader modLoader = new DirectoryModLoader(modLoaderDirectory);
		Mod mod = modLoader.load(modDirectory.getFileName().toString());
		
		ECSGame ecsGame = mod.createGame();
		assertEquals(1, ecsGame.getEntitiesWithComponent(PlayerComponent.class).size());
		
		modLoader.unload(modDirectory.getFileName().toString());
	}
	
	@Test(expected = SecurityException.class)
	public void testLoadModThatExitsJVM() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ModNotLoadableException {
		Path modLoaderDirectory = Files.createTempDirectory("modloader");
		modLoaderDirectory.toFile().deleteOnExit();
		
		Path modDirectory = Files.createTempDirectory(modLoaderDirectory, "exitingmod");
		Path compileDirectory = Files.createTempDirectory("compileDirectory");
		
		String simpleModSourceString = RuntimeJarHelper.createModSourceString(
			"ExitingMod", 
			"", 
			"System.exit(0);\n"
		);
		
		Path simpleModSource = compileDirectory.resolve("ExitingMod.java");
		Files.createFile(simpleModSource);
		Files.write(simpleModSource, simpleModSourceString.getBytes(StandardCharsets.UTF_8));
		
		List<Path> compiledModSources = RuntimeJarHelper.compileJavaSource(simpleModSource, compileDirectory);
		
		Path jarFile = modDirectory.resolve("exitingmod");
		Files.createFile(jarFile);
		RuntimeJarHelper.createJar(jarFile, compiledModSources);
		
		Properties properties = new Properties();
		properties.setProperty("language", "java");
		properties.setProperty("jar", jarFile.getFileName().toString());
		properties.setProperty("entryPoint", "com.cardshifter.core.integration.throwaway.runtimemod.ExitingMod");
		RuntimeJarHelper.createProperties(modDirectory, properties);
		
		ModLoader modLoader = new DirectoryModLoader(modLoaderDirectory);
		Mod mod = modLoader.load(modDirectory.getFileName().toString());
		
		ECSGame ecsGame = mod.createGame();
		assertEquals(0, ecsGame.getEntitiesWithComponent(PlayerComponent.class).size());
		
		modLoader.unload(modDirectory.getFileName().toString());
	}
	
	@Test(expected = SecurityException.class)
	public void testLoadModThatExitsJVMViaCallback() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ModNotLoadableException {
		Path modLoaderDirectory = Files.createTempDirectory("modloader");
		modLoaderDirectory.toFile().deleteOnExit();
		
		Path modDirectory = Files.createTempDirectory(modLoaderDirectory, "exitingviacallbackmod");
		Path compileDirectory = Files.createTempDirectory("compileDirectory");
		
		String simpleModSourceString = RuntimeJarHelper.createModSourceString(
			"ExitingViaCallbackMod", 
			new StringBuilder()
			.append("import com.cardshifter.modapi.base.ECSSystem;\n")
			.append("import com.cardshifter.modapi.events.StartGameEvent;\n")
			.toString(), 
			new StringBuilder()
			.append("game.addSystem(new ECSSystem() {\n")
			.append("    @Override\n")
			.append("    public void startGame(final ECSGame game) {\n")
			.append("		game.getEvents().registerHandlerAfter(this, StartGameEvent.class, event -> System.exit(0));\n")
			.append("    }\n")
			.append("});\n")
			.toString()
		);
		
		Path simpleModSource = compileDirectory.resolve("ExitingViaCallbackMod.java");
		Files.createFile(simpleModSource);
		Files.write(simpleModSource, simpleModSourceString.getBytes(StandardCharsets.UTF_8));
		
		List<Path> compiledModSources = RuntimeJarHelper.compileJavaSource(simpleModSource, compileDirectory);
		
		Path jarFile = modDirectory.resolve("exitingviacallbackmod");
		Files.createFile(jarFile);
		RuntimeJarHelper.createJar(jarFile, compiledModSources);
		
		Properties properties = new Properties();
		properties.setProperty("language", "java");
		properties.setProperty("jar", jarFile.getFileName().toString());
		properties.setProperty("entryPoint", "com.cardshifter.core.integration.throwaway.runtimemod.ExitingViaCallbackMod");
		RuntimeJarHelper.createProperties(modDirectory, properties);
		
		ModLoader modLoader = new DirectoryModLoader(modLoaderDirectory);
		Mod mod = modLoader.load(modDirectory.getFileName().toString());
		
		ECSGame ecsGame = mod.createGame();
		ecsGame.startGame();
		assertEquals(0, ecsGame.getEntitiesWithComponent(PlayerComponent.class).size());
		
		modLoader.unload(modDirectory.getFileName().toString());
	}
}
