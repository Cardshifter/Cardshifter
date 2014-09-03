package com.cardshifter.server.incoming;


public abstract class RequestMessage extends Message {
	
	public RequestMessage(String string) {
		super(string);
	}

	private int requestId;
	
	public final int getRequestId() {
		return requestId;
	}

}
