package com.cardshifter.api.messages;

/** 
 * Message class.
 * <p>
 * Takes a command to or from client in the form of a string.
 */
public abstract class Message {
	/** This command. */
	private String command;
	
	/**
	 * Constructor.
	 * @param string the command in the form of a character string
	 */
	public Message(String string) {
		this.command = string;
	}
	/**
	 * Getter.
	 * @return this command
	 */
	public final String getCommand() {
		return command;
	}
}
