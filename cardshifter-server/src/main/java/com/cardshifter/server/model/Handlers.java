package com.cardshifter.server.model;

import java.util.concurrent.atomic.AtomicReference;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.PlayCardMessage;
import com.cardshifter.server.incoming.PlayRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.outgoing.WaitMessage;
import com.cardshifter.server.outgoing.WelcomeMessage;

public class Handlers {

	private final Server server;
	
	public Handlers(Server server) {
		this.server = server;
	}

	public void loginMessage(LoginMessage message, ClientIO client) {
		
		if (message.getUsername().startsWith("x")) {
			client.sendToClient(new WelcomeMessage(false));
		}
		client.setName(message.getUsername());
		client.sendToClient(new WelcomeMessage(true));
		
	}

	public void playCard(PlayCardMessage message, ClientIO client) {
		
	}

	public void play(PlayRequest message, ClientIO client) {
		AtomicReference<ClientIO> playAny = server.getPlayAny();
		if (playAny.compareAndSet(null, client)) {
			
			client.sendToClient(new WaitMessage());
			
		}
		else {
			ClientIO opponent = playAny.getAndSet(null);
			
			server.newGame(client, opponent);
			
			// TODO: Start game with opponent vs client
		}
	}

	public void useAbility(UseAbilityMessage message, ClientIO client) {
		
	}

}
