package com.cardshifter.server.incoming;

import com.cardshifter.server.messages.Message;

public class LoginMessage extends Message {

	private String username;

	LoginMessage() {
		super("login");
	}
	
	public LoginMessage(String username) {
		this();
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
}
