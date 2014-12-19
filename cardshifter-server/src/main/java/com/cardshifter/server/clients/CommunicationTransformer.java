package com.cardshifter.server.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import com.cardshifter.api.messages.Message;

public interface CommunicationTransformer {

	void send(Message message, OutputStream out) throws IOException;
	void read(InputStream in, Consumer<Message> onReceived) throws IOException;
	
}
