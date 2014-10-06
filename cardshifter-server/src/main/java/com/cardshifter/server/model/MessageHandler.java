package com.cardshifter.server.model;

import com.cardshifter.api.messages.Message;

public interface MessageHandler<E extends Message> {

	void handle(E message, ClientIO client);
	
}
