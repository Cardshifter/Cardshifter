package com.cardshifter.server.model;

import com.cardshifter.api.*;
import com.cardshifter.api.messages.*;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.core.messages.*;

public class AuthorizationDecorator<T extends Message> implements MessageHandler<T> {

    private final MessageHandler<T> handler;

    public AuthorizationDecorator(MessageHandler<T> handler) {
        this.handler = handler;
    }

    @Override
    public void handle(T message, ClientIO client) {
        if (client.isLoggedIn()) {
            handler.handle(message, client);
        }
        else {
            client.sendToClient(new ErrorMessage("Login required", ErrorMessage.Cause.CLIENT));
        }
    }

}
