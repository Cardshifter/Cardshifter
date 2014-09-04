package com.cardshifter.server.incoming;

import com.cardshifter.server.abstr.CardMessage;

@Deprecated
public class PlayCardMessage extends CardMessage {
	
	public PlayCardMessage() {
		super("use");
	}

	private int cardId;
	
	public int getCardId() {
		return cardId;
	}
	
}
