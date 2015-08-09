package com.cardshifter.api;


public class UserNameAlreadyInUseException extends Exception {

    public UserNameAlreadyInUseException() {
    }

    public UserNameAlreadyInUseException(String message) {
        super(message);
    }

    public UserNameAlreadyInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNameAlreadyInUseException(Throwable cause) {
        super(cause);
    }

}
