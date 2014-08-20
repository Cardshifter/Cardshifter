package com.cardshifter.server.messages;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.annotation.JacksonInject;

public class ChatMessage extends Message {

	public ChatMessage(@JacksonInject Server server) {
		super(server);
	}

	@Override
	public void perform(ClientIO client) {
		
	}

}