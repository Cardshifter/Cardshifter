package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateMessage extends Message {

	private final int id;
	private final Object key;
	private final Object value;

	@JsonCreator
	public UpdateMessage(@JsonProperty("id") int id, @JsonProperty("key") Object key, @JsonProperty("value") Object value) {
		super("update");
		this.id = id;
		this.key = key;
		this.value = value;
	}

	public int getId() {
		return id;
	}
	
	public Object getKey() {
		return key;
	}
	
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "UpdateMessage [id=" + id + ", key=" + key + ", value=" + value + "]";
	}
	
}
