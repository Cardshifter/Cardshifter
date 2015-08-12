package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
/** Message that the server is waiting for an opponent to respond */
public class WaitMessage extends Message {

	private String message = "Waiting for opponent...";
	
	/** Constructor. (no params) */
	public WaitMessage() {
		super("wait");
	}
	/** @return  This wait message */
	public String getMessage() {
		return message;
	}
	
}
