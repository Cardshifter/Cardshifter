package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

import java.util.Arrays;

/** Message reporting server status. Sent in response to ServerQueryMessage.Request.STATUS */
public class ServerStatusMessage extends Message {

    private int users;
    private int ais;
    private int games;
    private String[] mods;

    /** Constructor. (no params) */
    public ServerStatusMessage() {
        this(0, 0, 0, new String[]{});
    }

    /**
     * Constructor.
     * @param users Number of online users (excluding AIs)
     * @param ais Number of AIs available to play with
     * @param games Number of games currently running
     * @param mods Names of the available mods
     */
    public ServerStatusMessage(int users, int ais, int games, String[] mods) {
        super("status");
        this.users = users;
        this.ais = ais;
        this.games = games;
        this.mods = mods;
    }

    /**
     * @return Number of available AIs
     */
    public int getAis() {
        return ais;
    }

    /**
     * @return Number of games running
     */
    public int getGames() {
        return games;
    }

    /**
     * @return The number of online users (excluding AIs)
     */
    public int getUsers() {
        return users;
    }

    /**
     * @return The names of the available mods
     */
    public String[] getMods() {
        return Arrays.copyOf(mods, mods.length);
    }

    /** @return  This message as converted to String */
    @Override
    public String toString() {
        return "ServerStatusMessage{" +
                "users=" + users +
                ", ais=" + ais +
                ", games=" + games +
                ", mods=" + Arrays.toString(mods) +
                '}';
    }
}
