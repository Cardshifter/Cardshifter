package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;

public class ResetAvailableActionsMessage extends Message {

	public ResetAvailableActionsMessage() {
		super("resetActions");
	}

}
