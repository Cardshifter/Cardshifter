package com.cardshifter.core.messages;

import com.cardshifter.api.ClientIO;
import com.cardshifter.api.messages.Message;

public interface MessageHandler<E extends Message> {

	void handle(E message, ClientIO client);
	
}
