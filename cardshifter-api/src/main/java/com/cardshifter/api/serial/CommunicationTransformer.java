package com.cardshifter.api.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.cardshifter.api.messages.Message;

public interface CommunicationTransformer {

	void send(Message message, OutputStream out) throws IOException;
	void read(InputStream in, MessageHandler onReceived) throws IOException;
	
}
