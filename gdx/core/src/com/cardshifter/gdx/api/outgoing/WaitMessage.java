package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

public class WaitMessage extends Message {

	private final String message = "Waiting for opponent...";
	
	public WaitMessage() {
		super("wait");
	}
	
	public String getMessage() {
		return message;
	}
	
}
