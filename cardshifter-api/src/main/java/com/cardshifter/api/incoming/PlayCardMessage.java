package com.cardshifter.api.incoming;

import com.cardshifter.api.abstr.CardMessage;

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
