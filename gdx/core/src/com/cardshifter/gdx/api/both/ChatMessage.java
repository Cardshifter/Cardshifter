package com.cardshifter.gdx.api.both;

import com.cardshifter.gdx.api.messages.Message;

public class ChatMessage extends Message {

	private int chatId;
	private String message;
	private String from;
	
	public ChatMessage() {
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

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
