package com.cardshifter.server.abstr;



public abstract class CardMessage extends RequestMessage {

	public CardMessage(String command) {
		super(command);
		// TODO: Add cardId to constructor args
	}

}
