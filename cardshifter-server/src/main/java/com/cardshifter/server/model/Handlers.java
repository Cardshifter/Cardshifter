package com.cardshifter.server.model;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import com.cardshifter.api.outgoing.AvailableModsMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.api.outgoing.UserStatusMessage.Status;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;

public class Handlers {

	private static final Logger logger = LogManager.getLogger(Handlers.class);
	private final Server server;
	
	public Handlers(Server server) {
		this.server = server;
	}

	public void query(ServerQueryMessage message, ClientIO client) {
		switch (message.getRequest()) {
			case USERS:
				for (ClientIO cl : server.getClients().values()) {
					client.sendToClient(new UserStatusMessage(cl.getId(), cl.getName(), Status.ONLINE));
				}
				break;
			default:
				client.sendToClient(new ServerErrorMessage("No such query request"));
				break;
		}
		
	}
	
	public void loginMessage(LoginMessage message, ClientIO client) {
		logger.info("Login request: " + message.getUsername() + " for client " + client);
		if (message.getUsername().startsWith("x")) {
			client.sendToClient(new WelcomeMessage(0, false));
			return;
		}
		logger.info("Client is welcome!");
		client.setName(message.getUsername());
		client.sendToClient(new WelcomeMessage(client.getId(), true));
		UserStatusMessage statusMessage = new UserStatusMessage(client.getId(), client.getName(), Status.ONLINE);
		server.getClients().values().stream()
			.filter(cl -> cl != client)
			.forEach(cl -> cl.sendToClient(statusMessage));
		server.getMainChat().add(client);
		Set<String> gameFactories = server.getGameFactories().keySet();
		client.sendToClient(new AvailableModsMessage(gameFactories.toArray(new String[gameFactories.size()])));
	}

	public void play(StartGameRequest message, ClientIO client) {
		if (message.getOpponent() < 0) {
			this.playAny(message, client);
		}
		else {
			ClientIO target = server.getClients().get(message.getOpponent());
			if (target == null) {
				logger.warn("Invite sent to unknown user: " + message);
				client.sendToClient(new InviteResponse(0, false));
				return;
			}
			
			ServerGame game = server.createGame(message.getGameType());
			ServerHandler<GameInvite> invites = server.getInvites();
			GameInvite invite = new GameInvite(invites, server.getMainChat(), client, game, message.getGameType());
			invites.add(invite);
			client.sendToClient(new WaitMessage());
			
			invite.sendInvite(target);
		}
	}
	
	public void inviteResponse(InviteResponse message, ClientIO client) {
		GameInvite invite = server.getInvites().get(message.getInviteId());
		if (invite != null) {
			invite.handleResponse(client, message.isAccepted());
		}
		else {
			logger.warn("No such invite: " + message.getInviteId());
		}
	}

	private void playAny(StartGameRequest message, ClientIO client) {
		AtomicReference<ClientIO> playAny = server.getPlayAny();
		if (playAny.compareAndSet(null, client)) {
			client.sendToClient(new WaitMessage());
		}
		else {
			ClientIO opponent = playAny.getAndSet(null);
			
			ServerGame game = server.createGame(message.getGameType());
			ServerHandler<GameInvite> invites = server.getInvites();
			GameInvite invite = new GameInvite(invites, server.getMainChat(), client, game, message.getGameType());
			invites.add(invite);
			invite.addPlayer(opponent);
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
