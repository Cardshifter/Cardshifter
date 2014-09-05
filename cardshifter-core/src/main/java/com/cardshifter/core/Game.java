package com.cardshifter.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.cardshifter.core.actions.UsableAction;

public class Game {
	public final LuaValue data = LuaValue.tableOf();
	
	private final List<Zone> zones = new ArrayList<>();
	private final List<Player> players = new ArrayList<>();
	private final Events events;
	private final Random random;
	private boolean gameOver = false;
	
	private Player currentPlayer;
	
	public Game(final InputStream file, final Random random) {
		Objects.requireNonNull(file, "file");
		Objects.requireNonNull(random, "random");
		this.events = new Events(file);
		this.random = random;
		
		this.players.add(new Player(this, "Player1"));
		this.players.add(new Player(this, "Player2"));
	}
	
	public Game(final InputStream file) {
		this(file, new Random());
	}
	
	public Player getCurrentPlayer() {
		return currentPlayer;
	}
	
	public List<Zone> getZones() {
		return zones;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public Player getFirstPlayer() {
		return players.get(0);
	}
	
	public Player getPlayer(int index) {
		return players.get(index);
	}
	
	public Player getLastPlayer() {
		return players.get(players.size() - 1);
	}
	
	public Events getEvents() {
		return events;
	}
	
	public Zone createZone(final Player owner, final String name) {
		Zone zone = new Zone(owner, name);
		this.zones.add(zone);
		return zone;
	}

	public List<UsableAction> getAllActions() {
		Stream<UsableAction> playerActions = getPlayers().stream()
			.flatMap(player -> player.getActions().values().stream());
		
		Stream<UsableAction> cardActions = getZones().stream()
			.flatMap(zone -> zone.getCards().stream())
			.flatMap(card -> card.getActions().values().stream());
		
		return Stream.concat(playerActions, cardActions).collect(Collectors.toList());
	}
	
	public void on(final String eventName, final LuaFunction function) {
		this.events.registerListener(eventName, function);
	}
	
	public void nextTurn() {
		//TODO is it not bad if currentPlayer == null?
		if (this.currentPlayer != null) {
			this.events.callEvent(Events.TURN_END, CoerceJavaToLua.coerce(this.currentPlayer), null);
		}
		
		this.currentPlayer = currentPlayer == null ? players.get(0) : currentPlayer.getNextPlayer();
				
		this.events.callEvent(Events.TURN_START, CoerceJavaToLua.coerce(this.currentPlayer), null);
	}
	
	public int randomInt(final int count) {
		return this.random.nextInt(count);
	}
	
	public Random getRandom() {
		return this.random;
	}
	
	public void setCurrentPlayer(final Player currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	
	public void gameOver() {
		this.gameOver = true;
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
}
