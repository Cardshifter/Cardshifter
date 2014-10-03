package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

public class ServerQueryMessage extends Message {

	public enum Request {
		USERS;
	}

	private final Request request;
	
	ServerQueryMessage() {
		this(Request.USERS);
	}
	public ServerQueryMessage(Request request) {
		super("query");
		this.request = request;
	}
	
	public Request getRequest() {
		return request;
	}
	
}
