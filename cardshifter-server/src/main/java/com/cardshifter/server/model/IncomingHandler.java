package com.cardshifter.server.model;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Handles incoming messages from clients in JSON format. For example: <code>{ "command": "something", "parameter": "example" }</code>
 * @author Simon Forsberg
 *
 */
public class IncomingHandler {
	
	private final Map<Class<?>, MessageHandler<?>> consumer;
	private final ObjectMapper mapper;
	private final ObjectReader reader;

	public IncomingHandler(Server server) {
		this.consumer = new ConcurrentHashMap<>();
		mapper = new ObjectMapper();
		Std inject = new InjectableValues.Std().addValue(Server.class, server);
		reader = mapper.reader(Message.class).with(inject);
	}

	/**
	 * Add the ability to parse a class. This causes commands passed to {@link #parse(String)} to be parsed as the specified class
	 * 
	 * @see #parse(String)
	 * 
	 * @param command Command-identifier to be supplied to the 'command' JSON parameter.
	 * @param handler The class to associate with this command identifier
	 */
	public <E extends Message> void addHandler(String command, Class<E> handler, MessageHandler<E> consumer) {
		this.consumer.put(handler, consumer);
	}
	
	/**
	 * Parse a JSON request
	 * 
	 * @param json JSON String to parse, formatted as <code>{ "command": "something", "parameter": "example" }</code> 
	 * @return A subclass of {@link Message} that is associated with the command identifier
	 * @throws IOException If there was a problem parsing the JSON
	 */
	public <T extends Message> T parse(String json) throws IOException {
		return reader.readValue(json);
	}

	public <E extends Message> void perform(E message, ClientIO client) {
		@SuppressWarnings("unchecked")
		MessageHandler<E> messagePerform = (MessageHandler<E>) this.consumer.get(message.getClass());
		messagePerform.handle(message, client);
	}

}
