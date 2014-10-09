
package com.cardshifter.core.modloader;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Frank van Heeswijk
 */
public abstract class AbstractMod implements Mod {
	protected final Path modDirectory;
	
	public AbstractMod(final Path modDirectory) {
		this.modDirectory = Objects.requireNonNull(modDirectory, "modDirectory");
	}
	
	@Override
	public String getName() {
		return modDirectory.getFileName().toString();
	}
}
