
package com.cardshifter.core.modloader;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Frank van Heeswijk
 */
public final class DirectoryModLoader implements ModLoader {
	private static final Map<String, ModLoadingFunction<Path, Mod, ModNotLoadableException>> LANGUAGE_MAPPING = new HashMap<>();
	static {
		LANGUAGE_MAPPING.put("java", JavaMod::new);
		LANGUAGE_MAPPING.put("lua", LuaMod::new);
	}
	
	private final Path modsDirectory;
	
	private final Map<String, Mod> loadedMods = new HashMap<>();
	
	public DirectoryModLoader(final Path modsDirectory) {
		this.modsDirectory = Objects.requireNonNull(modsDirectory, "modsDirectory");
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
			Mod mod = LANGUAGE_MAPPING.get(language).apply(modDirectory);
			loadedMods.put(modName, mod);
			return mod;
		} catch (Exception ex) {
			throw new ModNotLoadableException(ex);
		}
	}
	
	@Override
	public void unload(final String modName) {
		throw new UnsupportedOperationException();
//		  Objects.requireNonNull(modName, "modName");
//		  if (!loadedMods.containsKey(modName)) {
//			  throw new IllegalArgumentException("Mod " + modName + " has not been loaded");
//		  }
//		  loadedMods.remove(modName);
	}
	
	@Override
	public Map<String, Mod> getLoadedMods() {
		return loadedMods;
	}
}
