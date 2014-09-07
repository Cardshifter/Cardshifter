package com.cardshifter.server.model;

import java.util.Optional;
import java.util.Random;

import com.cardshifter.core.Card;
import com.cardshifter.core.Game;
import com.cardshifter.core.IdEntity;
import com.cardshifter.core.LuaTools;
import com.cardshifter.core.Player;
import com.cardshifter.core.TargetAction;
import com.cardshifter.core.UsableAction;
import com.cardshifter.core.Zone;
import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.outgoing.CardInfoMessage;
import com.cardshifter.server.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.server.outgoing.PlayerMessage;
import com.cardshifter.server.outgoing.UpdateMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.cardshifter.server.outgoing.ZoneMessage;

public class TCGGame extends ServerGame {

	private final Game game;
	
	public TCGGame(Server server, int id) {
		super(server, id);
		game = new Game(Game.class.getResourceAsStream("start.lua"), new Random(id), this::broadcast);
	}

	private void broadcast(IdEntity what, Object key, Object value) {
		if (getState() == GameState.NOT_STARTED) {
			// let the most information be sent when actually starting the game
			return;
		}
		
		if (what instanceof Card) {
			Card card = (Card) what;
			for (ClientIO io : this.getPlayers()) {
				Player player = playerFor(io);
				if (card.getZone().isKnownToPlayer(player)) {
					io.sendToClient(new UpdateMessage(card.getId(), key, value));
				}
			}
		}
		else {
			// Player, Zone, or Game
			this.send(new UpdateMessage(what.getId(), key, value));
		}
	}
	
	public void handleMove(UseAbilityMessage message, ClientIO client) {
		if (!this.getPlayers().contains(client)) {
			throw new IllegalArgumentException("Client is not in this game: " + client);
		}
		if (this.game.getCurrentPlayer() != playerFor(client)) {
			throw new IllegalArgumentException("It's not that players turn: " + client);
		}
		int entityId = message.getId();
		Optional<Player> player = game.getPlayers().stream().filter(pl -> pl.getId() == entityId).findFirst();
		Optional<Zone>   zone  = game.getZones().stream().filter(z -> z.getId() == entityId).findFirst();
		Optional<Card>   card   = game.getZones().stream().flatMap(z -> z.getCards().stream()).filter(c -> c.getId() == entityId).findFirst();
		
		String actionId = message.getAction();
		UsableAction action = null;
		if (player.isPresent()) {
			action = player.get().getActions().get(actionId);
		}
		if (zone.isPresent()) {
			throw new IllegalArgumentException("Id is zone " + zone.get() + " but does not have any actions.");
		}
		if (card.isPresent()) {
			action = card.get().getActions().get(actionId);
		}
		
		if (action == null) {
			throw new IllegalArgumentException("No such action was found.");
		}
		action.perform();
		// TODO: Add listener to game for ZoneMoves, inform players about card movements, and send CardInfoMessage when a card becomes known
		sendAvailableActions();
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
		game.getEvents().startGame(game);
		this.getPlayers().stream().forEach(pl -> this.send(new PlayerMessage(playerFor(pl).getName(), LuaTools.tableToJava(playerFor(pl).data))));
		this.game.getZones().stream().forEach(this::sendZone);
		this.sendAvailableActions();
	}
	
	private void sendAvailableActions() {
		for (ClientIO io : this.getPlayers()) {
			Player player = playerFor(io);
			io.sendToClient(new ResetAvailableActionsMessage());
			if (game.getCurrentPlayer() == player) {
				game.getAllActions().stream().filter(action -> action.isAllowed())
						.forEach(action -> io.sendToClient(new UseableActionMessage(action.getEntityId(), action.getName(), action instanceof TargetAction)));
				
			}
		}
	}

	private void sendZone(Zone zone) {
		for (ClientIO io : this.getPlayers()) {
			Player player = playerFor(io);
			io.sendToClient(constructZoneMessage(zone, player));
			if (zone.isKnownToPlayer(player)) {
				zone.getCards().forEach(card -> this.sendCard(io, card));
			}
		}
	}
	
	private ZoneMessage constructZoneMessage(Zone zone, Player player) {
		return new ZoneMessage(zone.getId(), zone.getName(), zone.getOwner().getIndex(), zone.size(), zone.isKnownToPlayer(player));
	}
	
	private void sendCard(ClientIO io, Card card) {
		io.sendToClient(new CardInfoMessage(card.getZone().getId(), card.getId(), LuaTools.tableToJava(card.data)));
	}

}
