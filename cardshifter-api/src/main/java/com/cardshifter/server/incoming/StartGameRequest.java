package com.cardshifter.server.incoming;

import com.cardshifter.server.messages.Message;

public class StartGameRequest extends Message {

	public StartGameRequest() {
		super("startgame");
	}
	
}
