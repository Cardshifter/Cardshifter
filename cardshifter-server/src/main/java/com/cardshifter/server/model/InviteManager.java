package com.cardshifter.server.model;

import com.cardshifter.api.*;
import com.cardshifter.core.game.*;

import java.util.*;
import java.util.concurrent.*;

public class InviteManager {

    private final Server server;
    private final ServerHandler<GameInvite> inviteHandler = new ServerHandler<>();
    private final Set<ClientIO> currentInvitors = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public InviteManager(Server server) {
        this.server = server;
    }

    private GameInvite createInvite(ClientIO sender, String gameType) {
        // Can only have one open invite at a time, see https://github.com/Cardshifter/Cardshifter/issues/373
        if (!currentInvitors.add(sender)) {
            return null;
        }

        ServerGame game = server.createGame(gameType);
        GameInvite invite = new GameInvite(this, server.getMainChat(), sender, game, gameType, inviteHandler.newId());
        inviteHandler.add(invite);

        return invite;
    }

    public void createAndSend(ClientIO sender, ClientIO receiver, String gameType) {
        GameInvite invite = createInvite(sender, gameType);
        if (invite != null) {
            invite.sendInvite(receiver);
        }
    }

    public void createAndAdd(ClientIO sender, ClientIO receiver, String gameType) {
        GameInvite invite = createInvite(sender, gameType);
        Objects.requireNonNull(invite);
        invite.addPlayer(receiver);
    }

    public void remove(GameInvite invite) {
        inviteHandler.remove(invite);
        currentInvitors.remove(invite.getHost());
    }

    public GameInvite getInvite(int id) {
        return inviteHandler.get(id);
    }

    public Collection<GameInvite> getInvites() {
        return inviteHandler.all().values();
    }

}
