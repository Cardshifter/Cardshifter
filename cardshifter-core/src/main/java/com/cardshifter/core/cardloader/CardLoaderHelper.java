
package com.cardshifter.core.cardloader;

import java.util.Locale;

/**
 *
 * @author Frank van Heeswijk
 */
final class CardLoaderHelper {
	private CardLoaderHelper() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Sanitizes the input of a tag, which can be a tag for example a resource or an attribute.
	 * 
	 * The tag will be converted to lower case, trimmed and underscores will be removed.
	 *
	 * @param tag	The tag to be sanitized
	 * @return	The sanitized tag
	 */
	static String sanitizeTag(final String tag) {
		return tag.toLowerCase(Locale.ENGLISH).trim().replace("_", "");
	}
}
