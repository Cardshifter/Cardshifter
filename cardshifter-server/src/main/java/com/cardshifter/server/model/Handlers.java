package com.cardshifter.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.cardshifter.ai.FakeAIClientTCG;
import com.cardshifter.api.*;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.core.username.*;
import com.cardshifter.server.clients.ClientSocketHandler;
import com.cardshifter.server.clients.ClientWebSocket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.UserStatusMessage.Status;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.core.game.FakeClient;
import com.cardshifter.core.game.TCGGame;

public class Handlers {

	private static final Logger logger = LogManager.getLogger(Handlers.class);
	private final Server server;
	
	public Handlers(Server server) {
		this.server = server;
	}

	public void query(ServerQueryMessage message, ClientIO client) {
		switch (message.getRequest()) {
			case USERS:
				List<ClientIO> clientsCopy = new ArrayList<>(server.getClients().values());
				Consumer<ClientIO> sendUser = cl -> client.sendToClient(new UserStatusMessage(cl.getId(), cl.getName(), Status.ONLINE));

				clientsCopy.stream()
					       .filter(ClientIO::isLoggedIn)
					       .forEach(sendUser);
				break;
			case DECK_BUILDER:
                Map<String, GameFactory> gameFactories = server.getGameFactories();
                if (message.getMessage() == null || !gameFactories.containsKey(message.getMessage())) {
                    client.sendToClient(ErrorMessage.client("Invalid gameType specified."));
                    return;
                }

				TCGGame game = (TCGGame) server.createGame(message.getMessage());
				game.addPlayer(client);
				game.addPlayer(new FakeClient(server, e -> {}));
				if (!game.preStartForConfiguration()) {
					client.sendToClient(ErrorMessage.client("There is no configuration required for mod " + message.getMessage()));
				}
				
				break;
            case STATUS:
                List<ClientIO> clients = server.getClients().values().stream()
                    .collect(Collectors.toList());
                Predicate<ClientIO> aiFilter = cl -> cl instanceof FakeAIClientTCG;
                int ais = (int) clients.stream().filter(aiFilter).count();
                int users = (int) clients.stream()
                    .filter(cl -> cl instanceof ClientWebSocket || cl instanceof ClientSocketHandler)
                    .count();

                int games = server.getGames().size();
                String[] mods = server.getGameFactories().keySet().stream().toArray(size -> new String[size]);
                client.sendToClient(new ServerStatusMessage(users, ais, games, mods));
                break;
			default:
				client.sendToClient(ErrorMessage.client("No such query request"));
				break;
		}
		
	}
	
	public void loginMessage(LoginMessage message, ClientIO client) {
		logger.info("Login request: " + message.getUsername() + " for client " + client);

		try {
			UserName name = UserName.create(message.getUsername());
			server.trySetClientName(client, name);
		}
		catch (UserNameAlreadyInUseException | InvalidUserNameException e) {
			client.sendToClient(ErrorMessage.client(e.getMessage()));
			return;
		}

		logger.info("Client is welcome!");
		client.sendToClient(new WelcomeMessage(client.getId(), "OK"));
		UserStatusMessage statusMessage = new UserStatusMessage(client.getId(), client.getName(), Status.ONLINE);
		server.getClients().values().stream()
			.filter(cl -> cl != client)
			.forEach(cl -> cl.sendToClient(statusMessage));
		server.getMainChat().add(client);
		Set<String> gameFactories = server.getGameFactories().keySet();
		client.sendToClient(new AvailableModsMessage(gameFactories.toArray(new String[gameFactories.size()])));
	}

	public void play(StartGameRequest message, ClientIO client) {
        if (message.getOpponent() == client.getId()) {
            client.sendToClient(ErrorMessage.client("You cannot invite yourself"));
            return;
        }
		if (message.getOpponent() < 0) {
			this.playAny(message, client);
		}
		else {
			ClientIO target = server.getClients().get(message.getOpponent());
			if (target == null) {
				logger.warn("Invite sent to unknown user: " + message);
				client.sendToClient(ErrorMessage.client("Invite sent to unknown user"));
				return;
			}
			
			server.getInviteManager().createAndSend(client, target, message.getGameType());
		}
	}
	
	public void inviteResponse(InviteResponse message, ClientIO client) {
		GameInvite invite = server.getInviteManager().getInvite(message.getInviteId());
		if (invite != null) {
			invite.handleResponse(client, message.isAccepted());
		}
		else {
			logger.warn("No such invite: " + message.getInviteId());
		}
	}

	private void playAny(StartGameRequest message, ClientIO client) {
		AtomicReference<ClientIO> playAny = server.getPlayAny();
		if (!playAny.compareAndSet(null, client)) {
			ClientIO opponent = playAny.getAndSet(null);
			server.getInviteManager().createAndAdd(client, opponent, message.getGameType());
		}
	}

	public void useAbility(UseAbilityMessage message, ClientIO client) {
		TCGGame game = (TCGGame) server.getGames().get(message.getGameId());
		game.handleMove(message, client);
	}

	public void requestTargets(RequestTargetsMessage message, ClientIO client) {
		TCGGame game = (TCGGame) server.getGames().get(message.getGameId());
		game.informAboutTargets(message, client);
	}

	public void chat(ChatMessage message, ClientIO client) {
		ChatArea chat = server.getChats().get(message.getChatId());
		if (message.getMessage().startsWith("/")) {
			client.sendToClient(new ChatMessage(message.getChatId(), "Command Handler", message.getMessage()));
			server.getCommandHandler().handle(new Command(client, message.getMessage().substring(1)));
		}
		else {
			chat.incomingMessage(message, client);
		}
	}
	
	public void incomingConfig(PlayerConfigMessage message, ClientIO client) {
		TCGGame game = (TCGGame) server.getGames().get(message.getGameId());
		game.incomingPlayerConfig(message, client);
	}
	
}
