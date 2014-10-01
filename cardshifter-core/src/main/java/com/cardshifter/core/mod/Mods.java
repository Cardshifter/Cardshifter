
package com.cardshifter.core.mod;

import java.nio.file.Path;
import java.util.Locale;

/**
 *
 * @author Frank van Heeswijk
 */
public final class Mods {
	private Mods() {
		throw new UnsupportedOperationException();
	}
	
	public static Mod open(final Path bootFile) {
		String filename = bootFile.getFileName().toString();
		String[] splits = filename.split("\\.");
		String extension = splits[splits.length - 1].toUpperCase(Locale.ENGLISH);
		return ExtensionMapping.valueOf(extension).getConstructor().apply(bootFile);
	}
}
