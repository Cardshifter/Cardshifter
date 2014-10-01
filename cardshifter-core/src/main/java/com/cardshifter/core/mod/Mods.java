
package com.cardshifter.core.mod;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Frank van Heeswijk
 */
public final class Mods {
	private final static Map<String, Function<Path, Mod>> LANGUAGE_MAPPING = new HashMap<>();
	static {
		LANGUAGE_MAPPING.put("java", JavaMod::new);
		LANGUAGE_MAPPING.put("lua", LuaMod::new);
	}
	
	private Mods() {
		throw new UnsupportedOperationException();
	}
	
	public static Mod open(final Path bootFile, final String language) {
		Objects.requireNonNull(bootFile, "bootFile");
		Objects.requireNonNull(language, "language");
		if (!LANGUAGE_MAPPING.containsKey(language)) {
			throw new IllegalArgumentException("Language " + language + " is not supported");
		}
		return LANGUAGE_MAPPING.get(language).apply(bootFile);
	}
}
