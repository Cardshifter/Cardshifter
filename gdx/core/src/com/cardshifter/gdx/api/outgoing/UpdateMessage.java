package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

public class UpdateMessage extends Message {

	private final int id;
	private final Object key;
	private final Object value;

	public UpdateMessage(int id, Object key, Object value) {
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
