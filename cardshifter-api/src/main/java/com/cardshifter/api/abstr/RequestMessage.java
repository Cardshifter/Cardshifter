package com.cardshifter.api.abstr;

import com.cardshifter.api.messages.Message;

/** Request message. */
public abstract class RequestMessage extends Message {
	/**
	 * Constructor.
	 * @param string  This message
	 */
	public RequestMessage(String string) {
		super(string);
	}

	private int requestId;
	/** @return  The Id of this request */
	public final int getRequestId() {
		return requestId;
	}

}
