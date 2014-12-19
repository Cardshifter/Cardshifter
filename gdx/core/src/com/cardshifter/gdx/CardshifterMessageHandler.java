package com.cardshifter.gdx;

import com.cardshifter.gdx.api.messages.Message;

public interface CardshifterMessageHandler {
    void handle(Message message);
}
