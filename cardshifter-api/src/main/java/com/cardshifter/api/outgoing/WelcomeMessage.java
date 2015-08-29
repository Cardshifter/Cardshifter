package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Response from the server after login */
public class WelcomeMessage extends Message {
	
	private static final int STATUS_OK = 200;
	private static final int STATUS_FAIL = 404;
	
	private int status;
	private int userId;
	private String message;

	/** Default constructor without params required for Jackson. */
	public WelcomeMessage() {
		this(-42, true, "");
	}

    /**
     * Constructor.
     * @param id  The Id of this user
     * @param success  Whether or not connection is successful
     * @param message  Resulting message to send to client
     */
	public WelcomeMessage(int id, boolean success, String message) {
		super("loginresponse");
        this.status = success ? STATUS_OK : STATUS_FAIL;
		this.message = message;
		this.userId = id;
	}

	/** @return  This message */
	public String getMessage() {
		return message;
	}

	/** @return  This status */
	public int getStatus() {
		return status;
	}

	/** @return  Whether connection status is successful */
	public boolean isOK() {
		return this.status == STATUS_OK;
	}

	/** @return  The Id of this user */
	public int getUserId() {
		return userId;
	}

	/** @return  This message as converted to String  */
	@Override
	public String toString() {
		return "WelcomeMessage ["
			+ "status=" + status 
			+ ", userId=" + userId
			+ ", message=" + message 
		+ "]";
	}
	
}
