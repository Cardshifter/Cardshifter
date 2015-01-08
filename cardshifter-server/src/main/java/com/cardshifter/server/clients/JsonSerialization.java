package com.cardshifter.server.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.serial.CommunicationTransformer;
import com.cardshifter.api.serial.MessageHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerialization implements CommunicationTransformer {

	private final ObjectMapper mapper;

	public JsonSerialization(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public void send(Message message, OutputStream out) throws IOException {
		mapper.writeValue(out, message);
	}

	@Override
	public void read(InputStream in, MessageHandler onReceived) throws IOException {
		MappingIterator<Message> values;
		values = mapper.readValues(new JsonFactory().createParser(in), Message.class);
		while (values.hasNextValue()) {
			Message message = values.next();
			if (!onReceived.messageReceived(message)) {
				return;
			}
		}
	}

}
