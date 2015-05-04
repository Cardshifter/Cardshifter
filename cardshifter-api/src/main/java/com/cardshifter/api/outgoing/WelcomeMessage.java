package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class WelcomeMessage extends Message {

	private static final int STATUS_OK = 200;
	
	private int status;
	private int userId;
	private String message;

	public WelcomeMessage() {
		this(-42, true);
	}
	public WelcomeMessage(int id, boolean success) {
		super("loginresponse");
		this.status = success ? STATUS_OK : 404;
		this.message = success ? "OK" : "Wrong username or password";
		this.userId = id;
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getStatus() {
		return status;
	}
	
	public boolean isOK() {
		return this.status == STATUS_OK;
	}
	
	public int getUserId() {
		return userId;
	}
	
	@Override
	public String toString() {
		return "WelcomeMessage [status=" + status + ", userId=" + userId
				+ ", message=" + message + "]";
	}
	
}
