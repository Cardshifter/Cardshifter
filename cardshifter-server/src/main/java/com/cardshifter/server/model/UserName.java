package com.cardshifter.server.model;

import java.util.*;

/**
 * Instances of this class are guaranteed to be valid user names
 */
public class UserName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;
    private static final Collection<String> reservedNames = Arrays.asList("Server", "undefined");

    private String name;

    private UserName(String name) {
        this.name = name;
    }

    /**
     * Try to create a new UserName object
     *
     * @param name Any string
     * @return A UserName guaranteed to be valid
     * @throws InvalidUserNameException If the proposed name is not valid
     */
    public static UserName create(String name) throws InvalidUserNameException {
        if (name.length() < MIN_LENGTH) {
            throw new InvalidUserNameException("Too short");
        }

        if (name.length() > MAX_LENGTH) {
            throw new InvalidUserNameException("Too long");
        }

        if (name.startsWith(" ") || name.endsWith(" ")) {
            throw new InvalidUserNameException("Starts or ends with space");
        }

        if (!name.matches("^[a-zA-Z0-9_ ]$")) {
            throw new InvalidUserNameException("Invalid characters");
        }

        if (reservedNames.contains(name)) {
            throw new InvalidUserNameException("Reserved");
        }

        return new UserName(name);
    }

    public String getString() {
        return name;
    }

}
