package com.cardshifter.gdx.api.incoming;

import com.cardshifter.gdx.api.messages.Message;

public class LoginMessage extends Message {

	private String username;

	public LoginMessage() {
		this("");
	}
	
	public LoginMessage(String username) {
		super("login");
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}

    public void setUsername(String username) {
        this.username = username;
    }
}
