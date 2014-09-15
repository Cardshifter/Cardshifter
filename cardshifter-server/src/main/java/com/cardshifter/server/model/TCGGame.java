package com.cardshifter.server.model;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.ai.CompleteIdiot;
import com.cardshifter.core.Card;
import com.cardshifter.core.Game;
import com.cardshifter.core.IdEntity;
import com.cardshifter.core.LuaTools;
import com.cardshifter.core.Player;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.Zone;
import com.cardshifter.core.actions.TargetAction;
import com.cardshifter.core.actions.UsableAction;
import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.RequestTargetsMessage;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.main.FakeAIClientTCG;
import com.cardshifter.server.outgoing.CardInfoMessage;
import com.cardshifter.server.outgoing.PlayerMessage;
import com.cardshifter.server.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.server.outgoing.UpdateMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.cardshifter.server.outgoing.ZoneMessage;

public class TCGGame extends ServerGame {
	
	private static final Logger logger = LogManager.getLogger(TCGGame.class);
	private static final long AI_DELAY_SECONDS = 5;
	private final Game game;
	private final ScheduledExecutorService aiPerform = Executors.newScheduledThreadPool(1);
	
	public TCGGame(Server server, int id) {
		super(server, id);
		game = new Game(TCGGame.class.getResourceAsStream("/com/cardshifter/mod/start.lua"), new Random(id), this::broadcast);
		aiPerform.scheduleWithFixedDelay(this::aiPerform, 0, AI_DELAY_SECONDS, TimeUnit.SECONDS);
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
	
	public void informAboutTargets(RequestTargetsMessage message, ClientIO client) {
		UsableAction action = findAction(message.getId(), message.getAction());
		TargetAction targetAction = (TargetAction) action;
		List<Targetable> targets = targetAction.findTargets();
//		client.sendToClient(new ResetAvailableActionsMessage()); // not sure if this should be sent or not
		for (Targetable target : targets) {
			IdEntity entity = (IdEntity) target;
			client.sendToClient(new UseableActionMessage(message.getId(), message.getAction(), false, entity.getId()));
		}
	}
	
	public Targetable findTargetable(int entityId) {
		Optional<Player> player = game.getPlayers().stream().filter(pl -> pl.getId() == entityId).findFirst();
		Optional<Card>	 card	= game.getZones().stream().flatMap(z -> z.getCards().stream()).filter(c -> c.getId() == entityId).findFirst();
		
		if (player.isPresent()) {
			return player.get();
		}
		if (card.isPresent()) {
			return card.get();
		}
		return null;
	}
	
	public UsableAction findAction(int entityId, String actionId) {
		Optional<Player> player = game.getPlayers().stream().filter(pl -> pl.getId() == entityId).findFirst();
		Optional<Zone>	 zone	= game.getZones().stream().filter(z -> z.getId() == entityId).findFirst();
		Optional<Card>	 card	= game.getZones().stream().flatMap(z -> z.getCards().stream()).filter(c -> c.getId() == entityId).findFirst();
		
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
		return action;
	}
	
	public void handleMove(UseAbilityMessage message, ClientIO client) {
		if (!this.getPlayers().contains(client)) {
			throw new IllegalArgumentException("Client is not in this game: " + client);
		}
		if (this.game.getCurrentPlayer() != playerFor(client)) {
			throw new IllegalArgumentException("It's not that players turn: " + client);
		}
		
		UsableAction action = findAction(message.getId(), message.getAction());
		if (action instanceof TargetAction) {
			TargetAction targetAction = (TargetAction) action;
			targetAction.setTarget(findTargetable(message.getTarget()));
		}
		action.perform();
		
		// TODO: Add listener to game for ZoneMoves, inform players about card movements, and send CardInfoMessage when a card becomes known
		sendAvailableActions();
	}

	private final CardshifterAI ai = new CompleteIdiot();
	
	private void aiPerform() {
		if (this.getState() != GameState.RUNNING) {
			return;
		}
		
		for (ClientIO io : this.getPlayers()) {
			if (io instanceof FakeAIClientTCG) {
				Player player = playerFor(io);
				UsableAction action = ai.getAction(player);
				if (action != null) {
					logger.info("AI Performs action: " + action);
					action.perform();
					sendAvailableActions();
					return;
				}
			}
		}
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
