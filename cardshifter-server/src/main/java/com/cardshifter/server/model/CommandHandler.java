package com.cardshifter.server.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class CommandHandler {
	
	private final Map<String, Consumer<Command>> commands;
	private final Map<String, ObjectReader> commandTypes;
	private final ObjectMapper mapper;
	private final InjectableValues inject;

	public CommandHandler(Server server) {
		this.commands = new ConcurrentHashMap<>();
		this.commandTypes = new ConcurrentHashMap<>();
		
		mapper = new ObjectMapper();
		inject = new InjectableValues.Std().addValue(Server.class, server);
	}

	public void addHandler(String command, Class<? extends Message> handler) {
		ObjectReader reader = mapper.reader(handler).with(inject);
		this.commandTypes.put(command, reader);
	}
	
	@Deprecated
	public void addHandler(String command, Consumer<Command> handler) {
		commands.put(command, handler);
	}
	
	public <T> T parse(String json) throws IOException {
	    ObjectMapper mapper = new ObjectMapper(); 
	    TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
	    HashMap<String, String> o = mapper.readValue(json, typeRef);

	    String command = o.get("command");
	    ObjectReader reader = commandTypes.get(command);
		return reader.readValue(json);
	}

	@Deprecated
	public boolean handle(Command command) {
		Consumer<Command> handler = commands.get(command.getCommand());
		if (handler == null) {
			System.out.println("Invalid command: " + command);
		}
		else handler.accept(command);
		return handler != null;
	}
	
}
