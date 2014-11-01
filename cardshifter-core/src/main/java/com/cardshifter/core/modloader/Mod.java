
package com.cardshifter.core.modloader;

import com.cardshifter.modapi.base.ECSMod;

/**
 *
 * @author Frank van Heeswijk
 */
public interface Mod extends ECSMod {
	/**
	 * Returns the name of the mod.
	 * 
	 * @return	The name of the mod
	 */
	String getName();
}
