package com.cardshifter.core.game;

import java.util.function.Consumer;

import com.cardshifter.api.ClientIO;
import com.cardshifter.api.ClientServerInterface;
import com.cardshifter.api.messages.Message;

public class FakeClient extends ClientIO {

	private final Consumer<Message> consumer;
	
	public FakeClient(ClientServerInterface server, Consumer<Message> consumer) {
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

	@Override
	public String getRemoteAddress() {
		return "Fake";
	}
	
}
