package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

/** 
 * Incoming login message.
 * <p>
 * A login message from a client to add a user to the available users on the server.
 * This login message is required before any other action or message can be performed between a client and a server.
 * @author Simon Forsberg 
 */
public class LoginMessage extends Message {
	
	/** User name <em>generally</em> input by a client user. */
	private String username;
	
	/** Constructor. (no params) */
	public LoginMessage() {
		this("");
	}
	
	/** 
	 * Constructor.
	 * @param   username  the incoming user name passed from client to server, not null
	 * @example Message:  <code>{ "command":"login","username":"JohnDoe" }</code>
	 */
	public LoginMessage(String username) {
		super("login");
		this.username = username;
	}
	
	/** @return the user name passed to the constructor */
	public String getUsername() {
		return username;
	}
}
