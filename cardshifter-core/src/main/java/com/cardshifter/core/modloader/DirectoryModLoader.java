
package com.cardshifter.core.modloader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author Frank van Heeswijk
 */
public final class DirectoryModLoader implements ModLoader {
	private static final Map<String, ModLoadingFunction<Path, LoadableMod, ModNotLoadableException>> LANGUAGE_MAPPING = new HashMap<>();
	static {
		LANGUAGE_MAPPING.put("java", JavaMod::new);
		LANGUAGE_MAPPING.put("lua", LuaMod::new);
	}
	
	private final Path modsDirectory;
	
	private final Map<String, LoadableMod> loadedMods = new HashMap<>();
	
	public DirectoryModLoader(final Path modsDirectory) {
		this.modsDirectory = Objects.requireNonNull(modsDirectory, "modsDirectory");
		if (!Files.isDirectory(modsDirectory)) {
			throw new IllegalArgumentException("modsDirectory " + modsDirectory + " must be a directory");
		}
	}
	
	@Override
	public Mod load(final String modName) throws ModNotLoadableException {
		Objects.requireNonNull(modName, "modName");
		if (loadedMods.containsKey(modName)) {
			throw new ModNotLoadableException("Mod " + modName + " has already been loaded");
		}
		try {
			Path modDirectory = modsDirectory.resolve(modName);
			Properties properties = ModLoaderHelper.getConfiguration(modDirectory);
			String language = properties.getProperty("language");
			if (!LANGUAGE_MAPPING.containsKey(language)) {
				throw new ModNotLoadableException("Language " + language + " is not supported");
			}
			LoadableMod loadableMod = LANGUAGE_MAPPING.get(language).apply(modDirectory);
			loadableMod.load();
			loadedMods.put(modName, loadableMod);
			return loadableMod;
		} catch (Exception ex) {
			throw new ModNotLoadableException(ex);
		}
	}
	
	@Override
	public void unload(final String modName) {
		  Objects.requireNonNull(modName, "modName");
		  if (!loadedMods.containsKey(modName)) {
			  throw new IllegalArgumentException("Mod " + modName + " has not been loaded");
		  }
		  loadedMods.get(modName).unload();
		  loadedMods.remove(modName);
	}
	
	@Override
	public Map<String, Mod> getLoadedMods() {
		Map<String, Mod> loadedModsCopy = new HashMap<>();
		loadedModsCopy.putAll(loadedMods);
		return loadedModsCopy;
	}

	/**
	 * Returns all mods that are available for play.
	 * 
	 * This method will return the names of all directories in the root modloader directory.
	 * 
	 * @return	A list containing the names of all available mods
	 */
	@Override
	public List<String> getAvailableMods() {
		try {
			return Files.list(modsDirectory)
				.filter(Files::isDirectory)
				.map(path -> path.getFileName().toString())
				.collect(Collectors.toList());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
