package com.cardshifter.api;

import com.cardshifter.api.messages.Message;

import java.util.Collection;

public interface ClientServerInterface {

	void handleMessage(ClientIO clientIO, String message);

	void performIncoming(Message message, ClientIO clientIO);

	void onDisconnected(ClientIO clientIO);

	int newClientId();

    LogInterface getLogger();

	Collection getClientNames();

}
