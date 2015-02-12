package com.cardshifter.gdx;

import com.cardshifter.api.messages.Message;

public interface SpecificHandler<T extends Message> {
    void handle(T message);
}
