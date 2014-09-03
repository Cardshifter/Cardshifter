package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class WelcomeMessage extends Message {

	private static final int STATUS_OK = 200;
	
	private final int status;
	private final String message;

	WelcomeMessage() {
		this(true);
	}
	public WelcomeMessage(boolean success) {
		super("loginresponse");
		this.status = success ? STATUS_OK : 404;
		this.message = success ? "OK" : "Wrong username or password";
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getStatus() {
		return status;
	}
	
	@JsonIgnore
	public boolean isOK() {
		return this.status == STATUS_OK;
	}
}
