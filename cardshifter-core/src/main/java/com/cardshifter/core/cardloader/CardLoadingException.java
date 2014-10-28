package com.cardshifter.core.cardloader;

/**
 *
 * @author Frank van Heeswijk
 */
public class CardLoadingException extends Exception {
	private static final long serialVersionUID = 64626564562564598L;
	
	public CardLoadingException() {
		
	}

	public CardLoadingException(final String message) {
		super(message);
	}

	public CardLoadingException(final Throwable cause) {
		super(cause);
	}

	public CardLoadingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
