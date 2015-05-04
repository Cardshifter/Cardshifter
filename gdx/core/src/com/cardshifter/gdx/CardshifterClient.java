package com.cardshifter.gdx;

import com.cardshifter.api.messages.Message;

public interface CardshifterClient {

    void send(Message message);

}
