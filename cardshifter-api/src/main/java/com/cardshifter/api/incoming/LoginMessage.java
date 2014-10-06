package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

public class LoginMessage extends Message {

	private final String username;

	LoginMessage() {
		this("");
	}
	
	public LoginMessage(String username) {
		super("login");
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
}
