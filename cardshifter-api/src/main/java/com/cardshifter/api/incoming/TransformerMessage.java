package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

/**
 * Serialize message from JSON to byte.
 * <p>
 * Primarily used for libGDX client. 
 */
public class TransformerMessage extends Message {

	public static final int TRANSFORM_JSON = 0;
	public static final int TRANSFORM_BYTE = 1;
	
	private final int type;
	
	/** Constructor. (no params) */
	TransformerMessage() {
		this(0);
	}
	/**
	 * Constructor.
	 * @param type  This message type
	 */
	public TransformerMessage(int type) {
		super("serial");
		this.type = type;
	}
	
	/** @return  This message type */
	public int getType() {
		return type;
	}
	
}
