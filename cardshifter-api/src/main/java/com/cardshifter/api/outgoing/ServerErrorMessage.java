package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerErrorMessage extends Message {

	private final String message;

	@JsonCreator
	public ServerErrorMessage(@JsonProperty("message") String message) {
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
