package com.cardshifter.server.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Handles incoming messages from clients in JSON format. For example: <code>{ "command": "something", "parameter": "example" }</code>
 * @author Simon Forsberg
 *
 */
public class IncomingHandler {
	
	private final Map<String, ObjectReader> commandTypes;
	private final Map<Class<?>, MessageHandler<?>> consumer;
	private final ObjectMapper mapper;
	private final InjectableValues inject;

	public IncomingHandler(Server server) {
		this.commandTypes = new ConcurrentHashMap<>();
		this.consumer = new ConcurrentHashMap<>();
		mapper = new ObjectMapper();
		inject = new InjectableValues.Std().addValue(Server.class, server);
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
		ObjectReader reader = mapper.reader(handler).with(inject);
		this.commandTypes.put(command, reader);
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
		ObjectMapper mapper = new ObjectMapper(); 
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
		HashMap<String, Object> o = mapper.readValue(json, typeRef);

		Object command = o.get("command");
		if (command == null || !commandTypes.containsKey(command)) {
			throw new UnsupportedOperationException("Command " + command + " is not supported. JSON: " + json);
		}
		ObjectReader reader = commandTypes.getOrDefault(command, null);
		return reader.readValue(json);
	}

	public <E extends Message> void perform(E message, ClientIO client) {
		@SuppressWarnings("unchecked")
		MessageHandler<E> messagePerform = (MessageHandler<E>) this.consumer.get(message.getClass());
		messagePerform.handle(message, client);
	}

}
