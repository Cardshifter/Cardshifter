package com.cardshifter.gdx;

import com.cardshifter.api.messages.Message;

public interface CardshifterMessageHandler {
    void handle(Message message);
}
