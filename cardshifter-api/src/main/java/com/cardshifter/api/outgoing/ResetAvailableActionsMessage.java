package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ResetAvailableActionsMessage extends Message {

	public ResetAvailableActionsMessage() {
		super("resetActions");
	}

}
