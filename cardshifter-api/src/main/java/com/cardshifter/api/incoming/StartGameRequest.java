package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

public class StartGameRequest extends Message {

	public StartGameRequest() {
		super("startgame");
	}
	
}
