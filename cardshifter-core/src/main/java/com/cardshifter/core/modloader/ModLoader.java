
package com.cardshifter.core.modloader;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Frank van Heeswijk
 */
public interface ModLoader {
	Mod load(final String modName) throws ModNotLoadableException;
	
	void unload(final String modName);
	
	List<String> getAvailableMods();
	
	Map<String, Mod> getLoadedMods();
}
