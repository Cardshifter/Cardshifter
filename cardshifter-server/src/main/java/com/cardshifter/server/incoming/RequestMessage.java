package com.cardshifter.server.incoming;

import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RequestMessage extends Message {
	
	public RequestMessage(Server server) {
		super(server);
	}
	
	@JsonProperty
	private int requestId;
	
	public final int getRequestId() {
		return requestId;
	}

}
