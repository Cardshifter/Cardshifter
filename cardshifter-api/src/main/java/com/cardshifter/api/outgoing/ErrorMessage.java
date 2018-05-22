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

	public static ErrorMessage client(String message) {
		return new ErrorMessage(message, Cause.CLIENT);
	}

	public static ErrorMessage server(String message) {
		return new ErrorMessage(message, Cause.SERVER);
	}

	public String getMessage() {
		return message;
	}

	/** Used when deserializing, see {@link net.zomis.cardshifter.ecs.usage.MixinErrorMessage}. */
	public void setCause(String name) {
		cause = Cause.valueOf(name.toUpperCase());
	}

	/** Used when serializing, see {@link net.zomis.cardshifter.ecs.usage.MixinErrorMessage}. */
	public String getCause() {
		return cause.toString().toLowerCase();
	}

	@Override
	public String toString() {
		return "ErrorMessage [message=" + message + ", cause=" + getCause() + "]";
	}

}
