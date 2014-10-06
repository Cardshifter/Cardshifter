package com.cardshifter.server.model;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;

public class ServerConsole extends ClientIO implements Runnable {

	public ServerConsole(Server server, CommandHandler commands) {
		super(server);
		this.commands = commands;
	}
	
	private final CommandHandler commands;
	
	public void addHandler(String command, Consumer<Command> handler) {
		commands.addHandler(command, handler);
	}
	
	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
		
		while (!Thread.interrupted()) {
			String input = scanner.nextLine();
			LogManager.getLogger(getClass()).info("Console input: " + input);
			Command cmd = new Command(this, input);
			boolean handled = commands.handle(cmd);
			if (!handled) {
				System.out.println("CONSOLE Invalid command: " + cmd);
			}
		}
		LogManager.getLogger(getClass()).info("Console stopped");
		scanner.close();
	}

	@Override
	public void onSendToClient(String message) {
		System.out.println(message);
	}

	@Override
	public void sentToServer(String message) {
		commands.handle(new Command(this, message));
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

}
