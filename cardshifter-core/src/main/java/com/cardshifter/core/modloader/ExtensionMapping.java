
package com.cardshifter.core.modloader;

import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Frank van Heeswijk
 */
enum ExtensionMapping {
	JAR(JavaMod::new);

	private final ModLoadingFunction<Path, Mod, ModNotLoadableException> constructor;
	
	private ExtensionMapping(final ModLoadingFunction<Path, Mod, ModNotLoadableException> constructor) {
		this.constructor = Objects.requireNonNull(constructor, "constructor");
	}

	ModLoadingFunction<Path, Mod, ModNotLoadableException> getConstructor() {
		return constructor;
	}
}
