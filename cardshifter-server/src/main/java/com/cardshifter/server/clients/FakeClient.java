package com.cardshifter.server.clients;

import java.util.function.Consumer;

import com.cardshifter.server.model.Server;

public class FakeClient extends ClientIO {

	private final Consumer<String> consumer;
	
	public FakeClient(Server server, Consumer<String> consumer) {
		super(server);
		this.consumer = consumer;
	}

	@Override
	public void onSendToClient(String message) {
		consumer.accept(message);
	}

	@Override
	public void close() {
		
	}
	
}
