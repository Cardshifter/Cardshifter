package com.cardshifter.server.outgoing;

import com.cardshifter.server.incoming.Message;

public class WelcomeMessage extends Message {

	private final int status;
	private final String message;

	public WelcomeMessage(boolean success) {
		this.status = success ? 200 : 404;
		this.message = success ? "OK" : "Wrong username or password";
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getStatus() {
		return status;
	}
}
