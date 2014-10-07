
package com.cardshifter.core.mod;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Frank van Heeswijk
 */
public abstract class BaseMod implements Mod {
	protected final Path bootFile;
	
	public BaseMod(final Path bootFile) {
		this.bootFile = Objects.requireNonNull(bootFile, "bootFile");
	}
}
