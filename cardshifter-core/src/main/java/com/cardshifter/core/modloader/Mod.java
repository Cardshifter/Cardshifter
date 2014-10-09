
package com.cardshifter.core.modloader;

import com.cardshifter.modapi.base.ECSGame;

/**
 *
 * @author Frank van Heeswijk
 */
public interface Mod {
	ECSGame createGame();
}
