package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class WaitMessage extends Message {

	private String message = "Waiting for opponent...";
	
	public WaitMessage() {
		super("wait");
	}
	
	public String getMessage() {
		return message;
	}
	
}
