package com.cardshifter.server.model;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.messages.Message;

public interface MessageHandler<E extends Message> {

	void handle(E message, ClientIO client);
	
}
