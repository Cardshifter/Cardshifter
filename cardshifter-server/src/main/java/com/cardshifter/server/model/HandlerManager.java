package com.cardshifter.server.model;

import com.cardshifter.api.*;
import com.cardshifter.api.both.*;
import com.cardshifter.api.incoming.*;
import com.cardshifter.api.messages.*;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.core.messages.*;
import org.apache.log4j.*;

import java.util.*;

public class HandlerManager {

    private static final Logger logger = LogManager.getLogger(HandlerManager.class);

    private final IncomingHandler incomingHandler;
    private final CommandHandler commandHandler;

    public HandlerManager(Server server) {
        incomingHandler = new IncomingHandler();
        commandHandler = new CommandHandler(server);

        Handlers handlers = new Handlers(server);

        addUnauthorizedHandler("login", LoginMessage.class, handlers::loginMessage);

        addHandler("chat", ChatMessage.class, handlers::chat);
        addHandler("startgame", StartGameRequest.class, handlers::play);
        addHandler("inviteResponse", InviteResponse.class, handlers::inviteResponse);
        addHandler("query", ServerQueryMessage.class, handlers::query);

        // Directly game-related
        addHandler("use", UseAbilityMessage.class, handlers::useAbility);
        addHandler("requestTargets", RequestTargetsMessage.class, handlers::requestTargets);
        addHandler("playerconfig", PlayerConfigMessage.class, handlers::incomingConfig);
    }

    public IncomingHandler getIncomingHandler() {
        return incomingHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public <E extends Message> void addHandler(String command, Class<E> handler, MessageHandler<E> consumer) {
        this.addUnauthorizedHandler(command, handler, new AuthorizationDecorator<>(consumer));
    }

    public <E extends Message> void addUnauthorizedHandler(String command, Class<E> handler, MessageHandler<E> consumer) {
        this.incomingHandler.addHandler(command, handler, consumer);
    }

    public void handleMessage(ClientIO client, String json) {
        Objects.requireNonNull(client, "Cannot handle message from a null client");
        logger.info("Handle message " + client + ": " + json);

        Message message;
        try {
            message = getIncomingHandler().parse(json);
            logger.info("Parsed Message: " + message);
            getIncomingHandler().perform(message, client);
        } catch (Exception e) {
            logger.error("Unable to parse incoming json: " + json, e);
            client.sendToClient(new ServerErrorMessage(e.getMessage()));
        }
    }
}
