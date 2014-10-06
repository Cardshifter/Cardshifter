package com.cardshifter.api.abstr;

import com.cardshifter.api.messages.Message;


public abstract class RequestMessage extends Message {
	
	public RequestMessage(String string) {
		super(string);
	}

	private int requestId;
	
	public final int getRequestId() {
		return requestId;
	}

}
