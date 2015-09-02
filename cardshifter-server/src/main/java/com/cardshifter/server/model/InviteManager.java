package com.cardshifter.server.model;

import com.cardshifter.api.*;
import com.cardshifter.core.game.*;

public class InviteManager {

    private final Server server;
    private final ServerHandler<GameInvite> inviteHandler = new ServerHandler<>();

    public InviteManager(Server server) {
        this.server = server;
    }

    public ServerHandler<GameInvite> getInviteHandler() {
        return inviteHandler;
    }

    public GameInvite createInvite(ClientIO sender, ClientIO receiver, String gameType) {
        ServerGame game = server.createGame(gameType);
        GameInvite invite = new GameInvite(inviteHandler, server.getMainChat(), sender, game, gameType);
        inviteHandler.add(invite);

        return invite;
    }

}
