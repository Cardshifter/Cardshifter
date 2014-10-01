
package com.cardshifter.core.mod;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Frank van Heeswijk
 */
enum ExtensionMapping {
	JAR(JavaMod::new),
	ZIP(LuaMod::new);
	
	private final Function<Path, Mod> constructor;
	
	private ExtensionMapping(final Function<Path, Mod> constructor) {
		this.constructor = Objects.requireNonNull(constructor, "constructor");
	}

	Function<Path, Mod> getConstructor() {
		return constructor;
	}
}
