package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ErrorMessage extends Message {

	public enum Cause {
		CLIENT, SERVER
	}

	private String message;
	private Cause cause;

	public ErrorMessage() {
		this("", Cause.SERVER);
	}

	/**
	 * @param message Error message text
	 * @param cause Who caused the error to occur?
	 */
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
