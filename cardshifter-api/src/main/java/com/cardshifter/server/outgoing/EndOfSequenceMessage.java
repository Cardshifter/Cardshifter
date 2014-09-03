package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;

public class EndOfSequenceMessage extends Message {

	public EndOfSequenceMessage() {
		super("eosq");
	}

}
