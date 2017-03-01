package com.cardshifter.server.model;

/**
 * Created by marc on 2017-02-25.
 */
public class NoModsLoadedException extends RuntimeException {
    public static String NO_MOD_MESSAGE = "No mod were loaded from the different folders. Please check your installation.";

    public NoModsLoadedException() {
        super(NO_MOD_MESSAGE);
    }
}
