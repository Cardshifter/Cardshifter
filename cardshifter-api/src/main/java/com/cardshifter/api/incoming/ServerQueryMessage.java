package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

/**
 * Make a specific type of request to the server. 
 * <p>
 * This is used to request an action from the server which requires server-side information.
 */
public class ServerQueryMessage extends Message {
	
	/**
	 * Types of request available.
	 * <li>USERS : Request list of available users.
	 * <li>DECK_BUILDER : Request the deck builder, which queries information both server-side (card sets) and client-side (saved decks).
	 */
	public enum Request {
		USERS, DECK_BUILDER;
	}

	private final Request request;
	private final String message;
	
	/** Constructor for USERS request. */
	ServerQueryMessage() {
		this(Request.USERS);
	}
	/**
	 * Constructor.
	 * @param request  This request
	 */
	public ServerQueryMessage(Request request) {
		this(request, "");
	}
	/**
	 * Constructor. 
	 * @param request  This request
	 * @param message  The message accompanying this request
	 */
	public ServerQueryMessage(Request request, String message) {
		super("query");
		this.request = request;
		this.message = message;
	}
	/** @return  This request */
	public Request getRequest() {
		return request;
	}
	/** @return  The message accompanying this request */
	public String getMessage() {
		return message;
	}
	/** @return  This request as converted to String */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": Request " + request + " message: " + message;
	}
}
