package com.cardshifter.server.model;

import com.cardshifter.api.*;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.core.game.*;
import org.apache.log4j.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Manages invites for the server and keeps track of pending invitations. All invites should be created through the
 * invite manager to be tracked.
 */
public class InviteManager {

    private static final Logger logger = LogManager.getLogger(InviteManager.class);

    private final Server server;
    private final ServerHandler<GameInvite> inviteHandler = new ServerHandler<>();
    private final Set<Integer> currentInvitors = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public InviteManager(Server server) {
        this.server = server;
    }

    /**
     * This is the one point where new GameInvites should be created.
     * @param sender Client who sent the invite
     * @param gameType Game type/mod
     * @return A new game invite
     */
    private GameInvite createInvite(ClientIO sender, String gameType) {
        // Can only have one open invite at a time, see https://github.com/Cardshifter/Cardshifter/issues/373
        if (!currentInvitors.add(sender.getId())) {
            logger.info("[" + sender + "] already has an open invitation");
            return null;
        }

        ServerGame game = server.createGame(gameType);
        GameInvite invite = new GameInvite(this, server.getMainChat(), sender, game, gameType, inviteHandler.newId());
        inviteHandler.add(invite);

        logger.info("Created [" + invite + "]");

        return invite;
    }

    /**
     * Create a new game invite and send it to a client.
     * @param sender Client who sent the invite
     * @param receiver Client the invite will be sent to
     * @param gameType Game type/mod
     */
    public void createAndSend(ClientIO sender, ClientIO receiver, String gameType) {
        GameInvite invite = createInvite(sender, gameType);
        if (invite != null) {
            logger.info("Sending [" + invite + "] to [" + receiver + "]");
            invite.sendInvite(receiver);
        } else {
            sender.sendToClient(new ErrorMessage("You already have a game invitation open."));
        }
    }

    /**
     * Create a new game invite and automatically add a client, as if they accepted it.
     * @param sender Client who sent the invite
     * @param receiver The invited client
     * @param gameType Game type/mod
     */
    public void createAndAdd(ClientIO sender, ClientIO receiver, String gameType) {
        GameInvite invite = createInvite(sender, gameType);
        Objects.requireNonNull(invite);
        invite.addPlayer(receiver);
    }

    public void remove(GameInvite invite) {
        logger.info("Removing [" + invite + "]");
        inviteHandler.remove(invite);
        currentInvitors.remove(invite.getHost().getId());
    }

    public GameInvite getInvite(int id) {
        return inviteHandler.get(id);
    }

    public Collection<GameInvite> getInvites() {
        return inviteHandler.all().values();
    }

}
