package com.cardshifter.core.replays;

public class ReplayException extends RuntimeException {

	private static final long serialVersionUID = 8212019092587249456L;

	public ReplayException(String message) {
		super(message);
	}
	
	public ReplayException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
