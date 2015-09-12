package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;
/** Message that available actions are reset. */
public class ResetAvailableActionsMessage extends Message {
	/** Constructor. (no params) */
	public ResetAvailableActionsMessage() {
		super("resetActions");
	}

}
