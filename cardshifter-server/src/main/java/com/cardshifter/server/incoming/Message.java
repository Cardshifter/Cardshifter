package com.cardshifter.server.incoming;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Message {

	private final Server server;
	
	@JsonProperty
	private String command;
	
	public Message(Server server) {
		this.server = server;
	}

	public final Server getServer() {
		return server;
	}

	public abstract void perform(ClientIO client);

	public final String getCommand() {
		return command;
	}
}
