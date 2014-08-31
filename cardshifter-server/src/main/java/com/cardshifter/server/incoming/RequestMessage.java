package com.cardshifter.server.incoming;


public abstract class RequestMessage extends Message {
	
	private int requestId;
	
	public final int getRequestId() {
		return requestId;
	}

}
