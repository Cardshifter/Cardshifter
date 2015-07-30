package com.cardshifter.api.messages;

/** 
 * Message class.
 * <p>
 * Takes a message type key to or from server in the form of a string.
 */
public abstract class Message {
	/** The message type */
	private String command;
	
	/**
	 * Constructor.
	 * @param string the key representing the type of message that is sent
	 */
	public Message(String string) {
		this.command = string;
	}
	/**
	 * Getter.
	 * @return string this message type
	 */
	public final String getCommand() {
		return command;
	}
}
