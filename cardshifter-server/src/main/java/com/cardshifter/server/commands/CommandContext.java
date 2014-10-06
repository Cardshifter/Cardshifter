package com.cardshifter.server.commands;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.Command;
import com.cardshifter.server.model.Server;

public class CommandContext {
	
	private final Server server;
	private final Command command;
	private final ClientIO client;

	public CommandContext(Server server, Command command, ClientIO client) {
		this.server = server;
		this.command = command;
		this.client = client;
	}
	
	public ClientIO getClient() {
		return client;
	}
	
	public Command getCommand() {
		return command;
	}
	
	public Server getServer() {
		return server;
	}

	public void sendChatResponse(String message) {
		client.sendToClient(new ChatMessage(1, "Server", message));
	}

}
