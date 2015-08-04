package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
/** Communicates a player's status */
public class UserStatusMessage extends Message {
	
	/**
	 * Possible statuses.
	 * <li>OFFLINE : User is not connected
	 * <li>ONLINE : User is connected
	 */
	public enum Status {
		OFFLINE, ONLINE;
	}
	
	private int userId;
	private Status status;
	private String name;
	
	/** Constructor. (no params) */
    	public UserStatusMessage() {
		this(-42, "", Status.OFFLINE);
	}
	/**
	 * Constructor.
	 * @param userId  The Id of this user
	 * @param name  The name of this user
	 * @param status  The status of this user
	 */
	public UserStatusMessage(int userId, String name, Status status) {
		super("userstatus");
		this.userId = userId;
		this.name = name;
		this.status = status;
	}
	/** @return  The Id of this user */
	public int getUserId() {
		return userId;
	}
	/** @return  The status of this user */
	public Status getStatus() {
		return status;
	}
	/** @return  The name of this user */
	public String getName() {
		return name;
	}
	/** @return  This message as converted to String  */
	@Override
	public String toString() {
		return "UserStatusMessage ["
			+ "userId=" + userId 
			+ ", status=" + status
			+ ", name=" + name 
		+ "]";
	}

}
