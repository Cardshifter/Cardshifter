package com.cardshifter.api.abstr;


/** Card message */
public abstract class CardMessage extends RequestMessage {
	/**
	 * Constructor.
	 * @param command  This command
	 */
	public CardMessage(String command) {
		super(command);
		// TODO: Add cardId to constructor args
	}

}
