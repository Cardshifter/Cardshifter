package com.cardshifter.api.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = Id.CUSTOM, property = "command", include = As.PROPERTY)
@JsonTypeIdResolver(MessageTypeIdResolver.class)
public abstract class Message {

	private final String command;

	public Message(String string) {
		this.command = string;
	}

	@JsonIgnore
	public final String getCommand() {
		return command;
	}
}
