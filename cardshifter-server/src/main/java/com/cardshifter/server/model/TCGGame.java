package com.cardshifter.server.model;

import java.util.Random;

import com.cardshifter.core.Card;
import com.cardshifter.core.Game;
import com.cardshifter.core.Player;
import com.cardshifter.core.Zone;
import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.outgoing.CardInfoMessage;
import com.cardshifter.server.outgoing.PlayerMessage;
import com.cardshifter.server.outgoing.ZoneMessage;

public class TCGGame extends ServerGame {

	private final Game game;
	
	public TCGGame(Server server, int id) {
		super(server, id);
		game = new Game(Game.class.getResourceAsStream("start.lua"), new Random(id));
	}

	@Override
	protected boolean makeMove(Command command, int player) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void updateStatus() {
		
	}

	public Player playerFor(ClientIO io) {
		int index = this.getPlayers().indexOf(io);
		if (index < 0) {
			throw new IllegalArgumentException(io + " is not a valid player in this game");
		}
		return this.game.getPlayers().get(index);
	}
	
	@Override
	protected void onStart() {
		this.getPlayers().stream().forEach(pl -> this.send(new PlayerMessage(playerFor(pl))));
		this.game.getZones().stream().forEach(this::sendZone);
	}
	
	private void sendZone(Zone zone) {
		for (ClientIO io : this.getPlayers()) {
			Player player = playerFor(io);
			io.sendToClient(new ZoneMessage(zone, player));
			if (zone.isKnownToPlayer(player)) {
				zone.getCards().forEach(this::sendCard);
			}
		}
	}
	
	private void sendCard(Card card) {
		this.send(new CardInfoMessage(card));
	}

}
