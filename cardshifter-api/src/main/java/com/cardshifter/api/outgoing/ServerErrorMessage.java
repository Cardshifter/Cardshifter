package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
/** Message reporting a server error. */
public class ServerErrorMessage extends Message {

	private String message;

	/** Constructor. (no params) */
    	public ServerErrorMessage() {
		this("");
	}
	/**
	 * Constructor.
	 * @param message  This error message
	 */
	public ServerErrorMessage(String message) {
		super("error");
		this.message = message;
	}
	/** @return  This error message */
	public String getMessage() {
		return message;
	}
	/** @return  This message as converted to String */
	@Override
	public String toString() {
		return "ServerErrorMessage [message=" + message + "]";
	}

}
