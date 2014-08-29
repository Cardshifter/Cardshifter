package com.cardshifter.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Game {

	private final List<Zone> zones;
	private final List<Player> players;
	private final Events events;
	private final Random random;
	public final LuaValue data;
	private boolean gameOver = false;
	private int turnNumber;
	private Player currentPlayer;
	
	public Game(InputStream file, Random random) {
		Objects.requireNonNull(random);
		Objects.requireNonNull(file);
		this.zones = new ArrayList<>();
		this.data = LuaValue.tableOf();
		this.players = new ArrayList<>();
		this.events = new Events(file);
		
		this.players.add(new Player(this, "Player1"));
		this.players.add(new Player(this, "Player2"));
		this.random = random;
                
                this.turnNumber = 0;
	}
	
	public Game(InputStream file) {
		this(file, new Random());
	}
        
        public int getTurnNumber() {
            return this.turnNumber;
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
	
	public Zone createZone(Player owner, String name) {
		Zone zone = new Zone(owner, name);
		this.zones.add(zone);
		return zone;
	}

	public List<UsableAction> getAllActions() {
		List<UsableAction> actions = new ArrayList<>();
		actions.addAll(getPlayers().stream().flatMap(player -> player.getActions().values().stream()).collect(Collectors.toList()));
		actions.addAll(getZones().stream().flatMap(zone -> zone.getCards().stream())
			.flatMap(card -> card.getActions().values().stream())
			.collect(Collectors.toList()));
		return actions;
	}
	
	public void on(String eventName, LuaFunction function) {
		this.events.registerListener(eventName, function);
	}
	
	public void nextTurn() {
		if (this.currentPlayer != null) {
			this.events.callEvent(Events.TURN_END, CoerceJavaToLua.coerce(this.currentPlayer), null);
		}
		
		this.currentPlayer = currentPlayer == null ? players.get(0) : currentPlayer.getNextPlayer();
				
		this.events.callEvent(Events.TURN_START, CoerceJavaToLua.coerce(this.currentPlayer), null);
                
                turnNumber++;
	}
	
	public int randomInt(int count) {
		return this.random.nextInt(count);
	}
	
	public Random getRandom() {
		return this.random;
	}
	
	public void setCurrentPlayer(Player currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	
	public void gameOver() {
		this.gameOver = true;
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
	
}
