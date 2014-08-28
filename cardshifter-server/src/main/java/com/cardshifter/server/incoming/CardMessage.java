package com.cardshifter.server.incoming;

import com.cardshifter.server.model.Server;

public abstract class CardMessage extends RequestMessage {

	public CardMessage(Server server) { // TODO: Add cardId to constructor args
		super(server);
	}

}
