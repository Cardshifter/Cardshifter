
package com.cardshifter.core.modloader;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Frank van Heeswijk
 */
public interface ModLoader {
	/**
	 * Loads a mod given its name, if the loading fails, an exception will be thrown.
	 * 
	 * @param modName	The name of the mod to be loaded
	 * @return	An instantation of the Mod interface
	 * @throws ModNotLoadableException	If the loading of the mod failed due to any reason
	 */
	Mod load(final String modName) throws ModNotLoadableException;
	
	/**
	 * Unloads a mod given its name.
	 * 
	 * @param modName	The name of the mod to be unloaded
	 */
	void unload(final String modName);
	
	/**
	 * Returns all mods that are available for play.
	 * 
	 * Please note: It may be possible that some mods present in the mod directory will not be available for play, if the ModLoader implementation allows to make mods unavailable to play.
	 * 
	 * @return	A list containing the names of all available mods
	 */
	List<String> getAvailableMods();
	
	/**
	 * Returns a map containing all loaded mods, where their name is mapped to the actual Mod instantiation.
	 * 
	 * @return	A map containing all loaded mods
	 */
	Map<String, Mod> getLoadedMods();
}
