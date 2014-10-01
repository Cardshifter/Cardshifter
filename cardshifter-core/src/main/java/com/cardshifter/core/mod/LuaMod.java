
package com.cardshifter.core.mod;

import java.nio.file.Path;

import net.zomis.cardshifter.ecs.base.ECSGame;

/**
 *
 * @author Frank van Heeswijk
 */
public class LuaMod extends BaseMod {
	public LuaMod(final Path bootFile) {
		super(bootFile);
	}
	
	@Override
	public ECSGame startGame() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
