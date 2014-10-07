package com.cardshifter.server.clients;

import java.util.function.Consumer;

import com.cardshifter.api.messages.Message;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.Server;

public class FakeClient extends ClientIO {

	private final Consumer<Message> consumer;
	
	public FakeClient(Server server, Consumer<Message> consumer) {
		super(server);
		this.consumer = consumer;
	}

	@Override
	public void onSendToClient(Message message) {
		consumer.accept(message);
	}

	@Override
	public void close() {
		
	}
	
}
