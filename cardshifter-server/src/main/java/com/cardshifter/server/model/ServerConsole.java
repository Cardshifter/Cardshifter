package com.cardshifter.server.model;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.log4j.LogManager;

import com.cardshifter.api.messages.Message;

public class ServerConsole extends ClientIO implements Runnable {

	public ServerConsole(Server server, CommandHandler commands) {
		super(server);
		this.commands = commands;
	}
	
	private final CommandHandler commands;
	
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
	public void onSendToClient(Message message) {
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

	@Override
	public String getRemoteAddress() {
		return "Console";
	}

}
