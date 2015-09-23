package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Response from the server signaling a successful login */
public class WelcomeMessage extends Message {

	private static final int STATUS_OK = 200;
	
	private int userId;
	private String message;

	/** Default constructor without params required for Jackson. */
	public WelcomeMessage() {
		this(-42, "");
	}

    /**
     * Constructor.
     * @param id  The Id of this user
     * @param message  Message to client
     */
	public WelcomeMessage(int id, String message) {
		super("loginresponse");
		this.message = message;
		this.userId = id;
	}

	/** @return  This message */
	public String getMessage() {
		return message;
	}

	/**
	 * Legacy method. A WelcomeMessage is always OK, but clients might depend this property.
	 * @return Always 200.
	 */
	public int getStatus() {
		return STATUS_OK;
	}

	/** @return  The Id of this user */
	public int getUserId() {
		return userId;
	}

	/** @return  This message as converted to String  */
	@Override
	public String toString() {
		return "WelcomeMessage ["
			+ ", userId=" + userId
			+ ", message=" + message 
		+ "]";
	}
	
}
