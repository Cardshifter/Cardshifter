package com.cardshifter.server.incoming;


public abstract class Message {

	private String command;

	public final String getCommand() {
		return command;
	}
}
