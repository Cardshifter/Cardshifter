
package com.cardshifter.core.cardloader;

/**
 *
 * @author Frank van Heeswijk
 */
class UncheckedCardLoadingException extends RuntimeException {
	private static final long serialVersionUID = 6644614691255149498L;

	UncheckedCardLoadingException() {
		
	}

	UncheckedCardLoadingException(final String message) {
		super(message);
	}

	UncheckedCardLoadingException(final Throwable cause) {
		super(cause);
	}

	UncheckedCardLoadingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
