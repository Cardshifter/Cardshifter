package com.cardshifter.api.serial;

import java.io.InputStream;
import java.io.OutputStream;

import com.cardshifter.api.CardshifterSerializationException;
import com.cardshifter.api.messages.Message;

public interface CommunicationTransformer {

	void send(Message message, OutputStream out) throws CardshifterSerializationException;
	void read(InputStream in, MessageHandler onReceived) throws CardshifterSerializationException;
	
}
