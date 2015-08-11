package com.cardshifter.server.model;


public class UserNameAlreadyInUseException extends Exception {

    UserNameAlreadyInUseException() {
        super("User name already in use by another client");
    }
}
