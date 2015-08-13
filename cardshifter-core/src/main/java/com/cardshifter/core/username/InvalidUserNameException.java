package com.cardshifter.core.username;

public class InvalidUserNameException extends Exception {

    public InvalidUserNameException() {
        super("User name invalid");
    }

    public InvalidUserNameException(String reason) {
        super("User name invalid: " + reason);
    }
}
