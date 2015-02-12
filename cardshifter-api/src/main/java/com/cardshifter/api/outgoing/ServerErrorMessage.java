package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ServerErrorMessage extends Message {

	private final String message;

	ServerErrorMessage() {
		this("");
	}

	public ServerErrorMessage(String message) {
		super("error");
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "ServerErrorMessage [message=" + message + "]";
	}

}
