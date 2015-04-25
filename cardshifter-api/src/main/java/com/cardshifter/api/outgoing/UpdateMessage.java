package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class UpdateMessage extends Message {

	private int id;
	private Object key;
	private Object value;

    public UpdateMessage() {
		this(0, "", 0);
	}

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
