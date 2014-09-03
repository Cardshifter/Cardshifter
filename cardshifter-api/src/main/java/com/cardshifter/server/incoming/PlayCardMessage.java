package com.cardshifter.server.incoming;

import com.cardshifter.server.abstr.CardMessage;

public class PlayCardMessage extends CardMessage {
	
	public PlayCardMessage() {
		super("play");
	}

	private int cardId;
	
	public int getCardId() {
		return cardId;
	}
	
}
