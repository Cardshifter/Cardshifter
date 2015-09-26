package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ErrorMessage extends Message {

	public enum Cause {
		CLIENT, SERVER
	}

	private String message;
	private Cause cause;

	public ErrorMessage() {
		this("");
	}

	/**
	 * @param message Error message text
	 */
	public ErrorMessage(String message) {
		this(message, Cause.SERVER);
	}

	public ErrorMessage(String message, Cause cause) {
		super("error");
		this.message = message;
		this.cause = cause;
	}

	public String getMessage() {
		return message;
	}

	public Cause getCause() {
		return cause;
	}

	@Override
	public String toString() {
		return "ErrorMessage [message=" + message + ", cause=" + cause + "]";
	}

}
