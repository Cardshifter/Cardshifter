package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Message stating an entity has been updated. */
public class UpdateMessage extends Message {

	private int id;
	private Object key;
	private Object value;
	/** Constructor. (no params) */
    	public UpdateMessage() {
		this(0, "", 0);
	}
	/**
	 * Constructor.
	 * @param id  The Id of this entity
	 * @param key  The key of this updated object
	 * @param value  The value that this object is updated to
	 */
	public UpdateMessage(int id, Object key, Object value) {
		super("update");
		this.id = id;
		this.key = key;
		this.value = value;
	}
	/** @return  The Id of this entity */
	public int getId() {
		return id;
	}
	/** @return  The key of this updated object */
	public Object getKey() {
		return key;
	}
	/** @return  The value that this object is updated to */
	public Object getValue() {
		return value;
	}
	/** @return  This message as converted to String */
	@Override
	public String toString() {
		return "UpdateMessage [id=" + id + ", key=" + key + ", value=" + value + "]";
	}
	
}
