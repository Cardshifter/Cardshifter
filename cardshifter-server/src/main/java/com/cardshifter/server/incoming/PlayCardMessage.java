package com.cardshifter.server.incoming;


public class PlayCardMessage extends CardMessage {
	
	public PlayCardMessage() {
		super("play");
	}

	private int cardId;
	
	public int getCardId() {
		return cardId;
	}
	
}
