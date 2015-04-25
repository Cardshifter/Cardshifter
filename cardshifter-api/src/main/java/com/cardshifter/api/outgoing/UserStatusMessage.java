package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class UserStatusMessage extends Message {

	public enum Status {
		OFFLINE, ONLINE;
	}
	
	private int userId;
	private Status status;
	private String name;

    public UserStatusMessage() {
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

	@Override
	public String toString() {
		return "UserStatusMessage [userId=" + userId + ", status=" + status
				+ ", name=" + name + "]";
	}

}
