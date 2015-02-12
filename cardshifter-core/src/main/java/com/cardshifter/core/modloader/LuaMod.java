
package com.cardshifter.core.modloader;

import java.nio.file.Path;

import com.cardshifter.modapi.base.ECSGame;

/**
 *
 * @author Frank van Heeswijk
 */
public class LuaMod extends LoadableMod {
	LuaMod(final Path modDirectory) {
		super(modDirectory);
	}

	@Override
	protected void load0() throws ModNotLoadableException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void unload0() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void createGame0(ECSGame game) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void declareConfiguration0(ECSGame game) {
		throw new UnsupportedOperationException();
	}
}
