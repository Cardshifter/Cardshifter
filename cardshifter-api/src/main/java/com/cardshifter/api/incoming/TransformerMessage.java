package com.cardshifter.api.incoming;

import com.cardshifter.api.messages.Message;

public class TransformerMessage extends Message {

	public static final int TRANSFORM_JSON = 0;
	public static final int TRANSFORM_BYTE = 1;
	
	private final int type;
	
	TransformerMessage() {
		this(0);
	}
	public TransformerMessage(int type) {
		super("serial");
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
}
