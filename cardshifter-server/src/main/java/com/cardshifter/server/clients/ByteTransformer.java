package com.cardshifter.server.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Predicate;

import com.cardshifter.api.messages.Message;

public class ByteTransformer implements CommunicationTransformer {

	@Override
	public void send(Message message, OutputStream out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void read(InputStream in, Predicate<Message> onReceived) throws IOException {
		throw new UnsupportedOperationException();
	}

}
