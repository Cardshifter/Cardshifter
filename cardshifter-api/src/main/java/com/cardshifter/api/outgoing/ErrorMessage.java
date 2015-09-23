package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ErrorMessage extends Message {

	private String message;

    	public ErrorMessage() {
		this("");
	}

	/**
	 * @param message Error message text
	 */
	public ErrorMessage(String message) {
		super("error");
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ErrorMessage [message=" + message + "]";
	}

}
