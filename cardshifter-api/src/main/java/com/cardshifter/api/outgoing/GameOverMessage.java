package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Message stating that this game is over. */
public class GameOverMessage extends Message {
	/** Constructor. (no params) */
	public GameOverMessage() {
		super("gameover");
	}
	
}
