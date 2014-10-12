package com.cardshifter.server.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.server.commands.CommandContext;

/**
 * Handles chat style commands such as '/example 1 2 something'
 * @author Simon Forsberg
 *
 */
public class CommandHandler {

	private static final Logger logger = LogManager.getLogger(CommandHandler.class);
	private final Map<String, CommandInfo<?>> commands;
	private final Server server;
	
	public static interface CommandHandle<T> {
		void handle(CommandContext command, T parameters);
	}
	
	public class CommandInfo<T> {
		
		private Supplier<T> supplier;
		private CommandHandle<T> handler;

		public CommandInfo(Supplier<T> supplier, CommandHandle<T> consumer) {
			this.supplier = supplier;
			this.handler = consumer;
		}
		
		public void handleCommand(Command cmd) {
			T obj = supplier.get();
			JCommander commander = new JCommander(obj);
			commander.parse(cmd.getAllParameters());
			handler.handle(new CommandContext(server, cmd, cmd.getSender()), obj);
		}
		
		public JCommander getCommander() {
			return new JCommander(supplier.get());
		}

		public String getDescription() {
			Parameters params = supplier.get().getClass().getAnnotation(Parameters.class);
			return params == null ? "(No description)" : params.commandDescription();
		}
		
	}
	
	public CommandHandler(Server server) {
		this.commands = new ConcurrentHashMap<>();
		this.server = server;
	}
	
	@Deprecated
	public void addHandler(String command, Consumer<Command> handler) {
		commands.put(command, new CommandInfo<Object>(() -> new Object(), (a, b) -> {}){
			@Override
			public void handleCommand(Command cmd) {
				handler.accept(cmd);
			}
		});
	}
	
	public void addHandler(String command, CommandInfo<?> handler) {
		commands.put(command, handler);
	}
	
	public <T> void addHandler(String command, Supplier<T> obj, CommandHandle<T> consumer) {
		this.addHandler(command, new CommandInfo<>(obj, consumer));
	}
	
	public boolean handle(Command command) {
		try {
			CommandInfo<?> handler = commands.get(command.getCommand());
			if (handler == null) {
				command.getSender().sendToClient(new ChatMessage(1, "Server", "Invalid command: " + command));
			}
			else {
				handler.handleCommand(command);
			}
			return handler != null;
		}
		catch (RuntimeException ex) {
			logger.error(ex.getMessage(), ex);
			return false;
		}
	}
	
	public Map<String, CommandInfo<?>> getCommands() {
		return Collections.unmodifiableMap(commands);
	}
	
}
