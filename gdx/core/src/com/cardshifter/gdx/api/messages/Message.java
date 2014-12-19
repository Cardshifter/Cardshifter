package com.cardshifter.gdx.api.messages;

public class Message {

    private final String command;

    public Message(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
