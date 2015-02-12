package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

public class ServerQueryMessage extends Message {

	public enum Request {
		USERS, DECK_BUILDER;
	}

	private final Request request;
	private final String message;
	
	ServerQueryMessage() {
		this(Request.USERS);
	}
	
	public ServerQueryMessage(Request request) {
		this(request, "");
	}
	
	public ServerQueryMessage(Request request, String message) {
		super("query");
		this.request = request;
		this.message = message;
	}
	
	public Request getRequest() {
		return request;
	}
	
	public String getMessage() {
		return message;
	}
	
}
