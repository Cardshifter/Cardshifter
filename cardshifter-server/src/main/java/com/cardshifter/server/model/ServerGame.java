package com.cardshifter.server.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.GameOverMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.modapi.base.ECSGame;

public abstract class ServerGame {
	private static final Logger logger = LogManager.getLogger(ServerGame.class);

	private final List<ClientIO> players;
	private final int id;

	private Instant active;
	private GameState state;
//	private final Set<ClientIO> observers;
//	private final ChatArea chat;
	
	public ServerGame(Server server, int id) {
		this.id = id;
		this.players = Collections.synchronizedList(new ArrayList<>());
		this.state = GameState.NOT_STARTED;
		this.active = Instant.now();
//		this.chat = server.newChatRoom(this.toString());
	}
	
	@Deprecated
	public boolean handleMove(Command command) {
		if (!players.contains(command.getSender())) {
			logger.warn("Game did not contain player " + command.getSender());
			return false;
		}
		int index = players.indexOf(command.getSender());
		logger.info("Command was received from index " + index + ": " + command.getSender().getName());
		this.active = Instant.now();
		return makeMove(command, index);
	}
	
	protected abstract boolean makeMove(Command command, int player);
	
	protected abstract void updateStatus();

	public void endGame() {
		if (state == GameState.ENDED) {
			throw new IllegalStateException("Game can only be ended once");
		}
		logger.info("Game Ended: " + this + " with players " + players);
		this.send(new GameOverMessage());
		this.active = Instant.now();
		this.state = GameState.ENDED;
	}

	public boolean isGameOver() {
		return state == GameState.ENDED;
	}
	
	public void start(List<ClientIO> players) {
		if (state != GameState.NOT_STARTED) {
			throw new IllegalStateException("Game can only be started once");
		}
		this.players.addAll(players);
		for (ClientIO player : players) {
			player.sendToClient(new NewGameMessage(this.id, players.indexOf(player)));
		}
		this.onStart();
		this.active = Instant.now();
		this.state = GameState.RUNNING;
	}

	protected abstract void onStart();
	
	public void send(Message data) {
		players.forEach(pl -> pl.sendToClient(data));
	}
	
	public int getId() {
		return id;
	}
	
	public Duration getLastActive() {
		return Duration.between(active, Instant.now());
	}
	
	public GameState getState() {
		return state;
	}
	
	public abstract ECSGame getGameModel();
	
	public List<ClientIO> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public boolean hasPlayer(ClientIO client) {
		return players.contains(client);
	}
	
}
