package com.cardshifter.api.both;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;


public class ChatMessage extends Message {

	private final int chatId;
	private final String message;
	private final String from;
	
	@JsonCreator
	ChatMessage() {
		this(0, "", "");
	}
	
	public ChatMessage(int chatId, String from, String message) {
		super("chat");
		this.chatId = chatId;
		this.from = from;
		this.message = message;
	}
	
	public int getChatId() {
		return chatId;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ChatMessage [chatId=" + chatId + ", message=" + message
				+ ", from=" + from + "]";
	}
	
}
