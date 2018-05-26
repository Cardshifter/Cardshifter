package com.cardshifter.core.game;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import com.cardshifter.core.modloader.GroovyMod;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ECSMod;

/**
 * Class where the Mods and AIs are initialized.
 * 
 * @author Simon Forsberg
 */
public class ModCollection {

	private static final Logger logger = LogManager.getLogger(ModCollection.class);
	
	/**
	 * All the AIs to initialize.
	 */
	private final Map<String, CardshifterAI> ais = new LinkedHashMap<>();
	
	/**
	 * All the mods to initialize.
	 */
	private final Map<String, Supplier<ECSMod>> mods = new LinkedHashMap<>();
	
	/**
	 * Initializes the AIs and Mods and puts them in the collections.
	 */
	public ModCollection() {
		ais.put("Loser", new ScoringAI(AIs.loser(), AIs.randomDeck(new Random())));
		ais.put("Idiot", new ScoringAI(AIs.idiot(), AIs.randomDeck(new Random())));
		ais.put("Medium", new ScoringAI(AIs.medium(), AIs::mediumDeck));
		ais.put("Fighter", new ScoringAI(AIs.fighter(), AIs::fighterDeck));
	}

    public static ModCollection defaultMods() {
        return new ModCollection().loadDefault();
    }

    public ModCollection loadDefault() {
        return loadExternal(new File("mods/").toPath());
    }
	
	/**
	 * Load all the external mods inside a directory
	 * 
	 * @param directory The directory to search for more mods.
	 */
	public ModCollection loadExternal(Path directory) {
        if (!Files.isDirectory(directory)) {
            logger.info(directory.toAbsolutePath() + " not found. External mod directory not loaded");
            return this;
        }
        logger.info("Loading mods in " + directory.toAbsolutePath());
        File[] groovyMods = directory.toFile().listFiles();
        if (groovyMods != null) {
            Arrays.stream(groovyMods)
                    .filter(File::isDirectory)
                    .filter(f -> new File(f, "Game.groovy").exists())
                    .peek(f -> logger.info("Loading mod " + f.getAbsolutePath()))
                    .forEach(f -> mods.put(f.getName(), () -> new GroovyMod(f, f.getName())));
        }
        return this;
	}
	
	/**
	 * 
	 * @return The CardshifterAI objects.
	 */
	public Map<String, CardshifterAI> getAIs() {
		return Collections.unmodifiableMap(ais);
	}

	/**
	 *
	 * @return A set containing the names of available mods
	 */
	public Set<String> getAvailableMods() {
		return Collections.unmodifiableSet(mods.keySet());
	}

	/**
	 * Get a mod object for the specified name
	 *
	 * @param name Name of the mod
	 * @return Mod object
	 */
	public ECSMod getModFor(String name) {
		Supplier<ECSMod> supplier = mods.get(name);
		return supplier == null ? null : supplier.get();
	}

	public Path getDefaultModLocation() {
		return new File(System.getProperty("user.home"), "cardshifter-mods").toPath();
	}
	
}
