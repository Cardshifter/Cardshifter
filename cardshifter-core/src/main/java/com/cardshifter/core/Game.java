package com.cardshifter.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.cardshifter.core.actions.UsableAction;

public class Game implements IdEntity {

	public static interface StateChangeListener {
		void onChange(IdEntity what, Object key, Object value);
	}
	
	public final LuaValue data = new ExtLuaTable((key, value) -> this.broadcastChange(this, key, value));
	
	private final List<Zone> zones = new ArrayList<>();
	private final List<Player> players = new ArrayList<>();
	private final Events events;
	private final Random random;
	
	private boolean gameOver = false;
	private final AtomicInteger ids;
	private int turnNumber;
	
	private Player currentPlayer;
	private final StateChangeListener listener;
	private final int id;
	
	public Game(InputStream file, Random random) {
		this(file, random, null);
	}
	
	public Game(InputStream file, Random random, StateChangeListener listener) {
		Objects.requireNonNull(file, "file");
		Objects.requireNonNull(random, "random");
		this.ids = new AtomicInteger(0);
		this.id = nextId();
		this.events = new Events(file);
		this.random = random;
		
		this.players.add(new Player(this, "Player1", nextId()));
		this.players.add(new Player(this, "Player2", nextId()));
		this.listener = listener;
		this.turnNumber = 1;
	}
	
	public Game(final InputStream file) {
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
	
	public Zone createZone(final Player owner, final String name) {
		Zone zone = new Zone(owner, name, this.nextId());
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

                //Quick hack to only advance the turn number when control passes back to player
		if (this.currentPlayer == this.getLastPlayer()) {
                    turnNumber++;
                }
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

	int nextId() {
		return this.ids.getAndIncrement();
	}

	void broadcastChange(IdEntity what, Object key, Object value) {
		if (this.listener != null) {
			this.listener.onChange(what, key, value);
		}
	}
	
	@Override
	public int getId() {
		return id;
	}
	
}
