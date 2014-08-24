package com.cardshifter.server.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cardshifter.server.messages.Message;
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
	private final ObjectMapper mapper;
	private final InjectableValues inject;

	public IncomingHandler(Server server) {
		this.commandTypes = new ConcurrentHashMap<>();
		
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
	public void addHandler(String command, Class<? extends Message> handler) {
		ObjectReader reader = mapper.reader(handler).with(inject);
		this.commandTypes.put(command, reader);
	}
	
	/**
	 * Parse a JSON request
	 * 
	 * @param json JSON String to parse, formatted as <code>{ "command": "something", "parameter": "example" }</code> 
	 * @return A subclass of {@link Message} that is associated with the command identifier
	 * @throws IOException If there was a problem parsing the JSON
	 */
	public <T> T parse(String json) throws IOException {
	    ObjectMapper mapper = new ObjectMapper(); 
	    TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
	    HashMap<String, String> o = mapper.readValue(json, typeRef);

	    String command = o.get("command");
	    ObjectReader reader = commandTypes.get(command);
		return reader.readValue(json);
	}

}
