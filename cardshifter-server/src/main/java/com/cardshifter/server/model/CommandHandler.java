package com.cardshifter.server.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.server.commands.CommandContext;

/**
 * Handles chat style commands such as '/example 1 2 something'
 * @author Simon Forsberg
 *
 */
public class CommandHandler {

	private static final Logger logger = LogManager.getLogger(CommandHandler.class);
	private final Map<String, Consumer<Command>> commands;
	private final Server server;
	
	public static interface CommandHandle<T> {
		void handle(CommandContext command, T parameters);
	}
	
	public CommandHandler(Server server) {
		this.commands = new ConcurrentHashMap<>();
		this.server = server;
	}
	
	public void addHandler(String command, Consumer<Command> handler) {
		commands.put(command, handler);
	}
	
	public <T> void addHandler(String command, Supplier<T> obj, CommandHandle<T> consumer) {
		this.addHandler(command, cmd -> handle(cmd, obj, consumer));
	}
	
	private <T> void handle(Command cmd, Supplier<T> supplier, CommandHandle<T> consumer) {
		T obj = supplier.get();
		JCommander commander = new JCommander(obj);
		commander.parse(cmd.getAllParameters());
		consumer.handle(new CommandContext(server, cmd, cmd.getSender()), obj);
	}
	
	public boolean handle(Command command) {
		try {
			Consumer<Command> handler = commands.get(command.getCommand());
			if (handler == null) {
				command.getSender().sendToClient(new ChatMessage(1, "Server", "Invalid command: " + command));
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
