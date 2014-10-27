package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.api.messages.MessageTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = Id.CUSTOM, property = "command", include = As.PROPERTY)
@JsonTypeIdResolver(MessageTypeIdResolver.class)
public class MixinMessage {

	private final String command;

	public MixinMessage(String string) {
		this.command = string;
	}

	@JsonIgnore
	public final String getCommand() {
		return command;
	}
}
