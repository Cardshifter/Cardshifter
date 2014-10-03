package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class UserStatusMessage extends Message {

	public enum Status {
		OFFLINE, ONLINE;
	}
	
	private final int userId;
	private final Status status;
	private final String name;

	UserStatusMessage() {
		this(-42, "", Status.OFFLINE);
	}
	
	public UserStatusMessage(int userId, String name, Status status) {
		super("userstatus");
		this.userId = userId;
		this.name = name;
		this.status = status;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public String getName() {
		return name;
	}
	
}
