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
	 * @param chatId  The Id of the Chat Area of this message, not null; always 1 unless there are muliple chat areas
	 * @param from  The name of the sender of this message, not null; should be "unused" when sent from client to server; the server will populate this value by itself when broadcasting the message
	 * @param message  The content of this chat message
	 *
	 * @example <code>{"chatId":1,"from":"unused","message":"Hello, World!","command":"chat"}</code>
	 */
	public ChatMessage(int chatId, String from, String message) {
		super("chat");
		this.chatId = chatId;
		this.from = from;
		this.message = message;
	}
	/** @return  The Id of the Chat Area of this message */
	public int getChatId() {
		return chatId;
	}
	/** @return  The name of the sender of this message */
	public String getFrom() {
		return from;
	}
	/** @return  The content of this message */
	public String getMessage() {
		return message;
	}
	/**
	 * @return  This message as converted to String
	 *
	 * @example <code>ChatMessage [chatId=1, message=Hello, World!, from=unused]</code>
	 */
	@Override
	public String toString() {
		return "ChatMessage ["
			+ "chatId=" + chatId
			+ ", message=" + message
			+ ", from=" + from
		+ "]";
	}
	
}
