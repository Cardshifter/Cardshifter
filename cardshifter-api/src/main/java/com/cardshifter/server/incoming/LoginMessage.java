package com.cardshifter.server.incoming;

public class LoginMessage extends Message {

	private String username;

	public LoginMessage(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
}
