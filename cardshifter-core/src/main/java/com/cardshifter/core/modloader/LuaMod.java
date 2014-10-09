
package com.cardshifter.core.modloader;

import java.nio.file.Path;

import com.cardshifter.modapi.base.ECSGame;

/**
 *
 * @author Frank van Heeswijk
 */
public class LuaMod extends AbstractMod {
	public LuaMod(final Path modDirectory) {
		super(modDirectory);
	}
	
	@Override
	public ECSGame createGame() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
