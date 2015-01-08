package com.cardshifter.api.serial;

import com.cardshifter.api.messages.Message;

public interface MessageHandler {

	boolean messageReceived(Message message);
	
}
