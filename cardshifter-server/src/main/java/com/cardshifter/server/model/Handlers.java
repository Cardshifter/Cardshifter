package com.cardshifter.server.model;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.RequestTargetsMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.outgoing.WaitMessage;
import com.cardshifter.server.outgoing.WelcomeMessage;

public class Handlers {

	private static final Logger logger = LogManager.getLogger(Handlers.class);
	private final Server server;
	
	public Handlers(Server server) {
		this.server = server;
	}

	public void loginMessage(LoginMessage message, ClientIO client) {
		logger.info("Login request: " + message.getUsername() + " for client " + client);
		if (message.getUsername().startsWith("x")) {
			client.sendToClient(new WelcomeMessage(false));
			return;
		}
		logger.info("Client is welcome!");
		client.setName(message.getUsername());
		client.sendToClient(new WelcomeMessage(true));
	}

	public void play(StartGameRequest message, ClientIO client) {
		AtomicReference<ClientIO> playAny = server.getPlayAny();
		if (playAny.compareAndSet(null, client)) {
			client.sendToClient(new WaitMessage());
		}
		else {
			ClientIO opponent = playAny.getAndSet(null);
			server.newGame(client, opponent);
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

}
