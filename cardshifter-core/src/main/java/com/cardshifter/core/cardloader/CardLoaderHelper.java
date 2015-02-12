
package com.cardshifter.core.cardloader;

import java.util.Arrays;
import java.util.List;
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
	
	/**
	 * Returns the tags that are required when loading cards.
	 * 
	 * @return	The required tags
	 */
	static List<String> requiredTags() {
		return Arrays.asList("id");
	}
}
