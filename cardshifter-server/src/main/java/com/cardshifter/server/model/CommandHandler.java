package com.cardshifter.server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Handles chat style commands such as '/example 1 2 something'
 * @author Simon Forsberg
 *
 */
public class CommandHandler {

	private final Map<String, Consumer<Command>> commands;
	
	public CommandHandler() {
		this.commands = new ConcurrentHashMap<>();
	}
	
	public void addHandler(String command, Consumer<Command> handler) {
		commands.put(command, handler);
	}
	
	public boolean handle(Command command) {
		Consumer<Command> handler = commands.get(command.getCommand());
		if (handler == null) {
			command.getSender().sendToClient("Invalid command: " + command);
		}
		else handler.accept(command);
		return handler != null;
	}
	
}
