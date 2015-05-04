package com.cardshifter.api.messages;

public abstract class Message {

	private String command;

	public Message(String string) {
		this.command = string;
	}

	public final String getCommand() {
		return command;
	}
}
