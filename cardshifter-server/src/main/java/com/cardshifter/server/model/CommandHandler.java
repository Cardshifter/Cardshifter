package com.cardshifter.server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Handles chat style commands such as '/example 1 2 something'
 * @author Simon Forsberg
 *
 */
public class CommandHandler {

	private static final Logger logger = LogManager.getLogger(CommandHandler.class);
	private final Map<String, Consumer<Command>> commands;
	
	public CommandHandler() {
		this.commands = new ConcurrentHashMap<>();
	}
	
	public void addHandler(String command, Consumer<Command> handler) {
		commands.put(command, handler);
	}
	
	public boolean handle(Command command) {
		try {
			Consumer<Command> handler = commands.get(command.getCommand());
			if (handler == null) {
				command.getSender().sendToClient("Invalid command: " + command);
			}
			else {
				handler.accept(command);
			}
			return handler != null;
		}
		catch (RuntimeException ex) {
			logger.error(ex.getMessage(), ex);
			return false;
		}
	}
	
}
