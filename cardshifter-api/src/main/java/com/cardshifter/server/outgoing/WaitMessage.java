package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;

public class WaitMessage extends Message {

	private final String message = "Waiting for opponent...";
	
	public WaitMessage() {
		super("wait");
	}
	
	public String getMessage() {
		return message;
	}
	
}
