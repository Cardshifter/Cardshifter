package com.cardshifter.server.model;

public class InvalidUserNameException extends Exception {

    public InvalidUserNameException() {
        super("User name is invalid");
    }
}
