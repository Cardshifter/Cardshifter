package com.cardshifter.api;

/**
 * Created by Simon on 4/25/2015.
 */
public class CardshifterSerializationException extends Exception {

    public CardshifterSerializationException() {
    }

    public CardshifterSerializationException(String message) {
        super(message);
    }

    public CardshifterSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardshifterSerializationException(Throwable cause) {
        super(cause);
    }

}
