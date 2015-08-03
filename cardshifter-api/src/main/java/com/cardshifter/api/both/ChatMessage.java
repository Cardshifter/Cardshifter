package com.cardshifter.api.both;

import com.cardshifter.api.messages.Message;

/**
 * Chat message in game lobby.
 * <p>
 * These are messages printed to the game lobby which are visible to all users present at the time the message is posted.
 */
public class ChatMessage extends Message {

	private int chatId;
	private String message;
	private String from;
	
	/** Constructor. (no params) */
	public ChatMessage() {
		this(0, "", "");
	}
	/**
	 * Constructor.
	 * @param chatId  The Id of this chat message
	 * @param from  The Id of the sender of this message
	 * @param message  The content of this chat message
	 */
	public ChatMessage(int chatId, String from, String message) {
		super("chat");
		this.chatId = chatId;
		this.from = from;
		this.message = message;
	}
	/** @return  The Id of this chat message */
	public int getChatId() {
		return chatId;
	}
	/** @return  The Id of the sender of this message */
	public String getFrom() {
		return from;
	}
	/** @return  The content of this message */
	public String getMessage() {
		return message;
	}
	/** @return  This message as converted to String */
	@Override
	public String toString() {
		return "ChatMessage [chatId=" + chatId + ", message=" + message
				+ ", from=" + from + "]";
	}
	
}
