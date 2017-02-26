package com.cardshifter.server.model;

/**
 * Created by marc on 2017-02-25.
 */
public class NoModsLoadedException extends RuntimeException {
    public NoModsLoadedException(String message) {
        super(message);
    }
}
