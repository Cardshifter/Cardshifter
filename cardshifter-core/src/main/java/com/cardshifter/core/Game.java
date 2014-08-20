package com.cardshifter.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.luaj.vm2.LuaValue;

public class Game {

	private final List<Zone> zones;
	private final List<Player> players;
	private final Events events;
	public final LuaValue data;
	
	public Game(InputStream file) {
		this.zones = new ArrayList<>();
		this.data = LuaValue.tableOf();
		this.players = new ArrayList<>();
		this.events = new Events(file);
		
		this.players.add(new Player(this, "Player1"));
		this.players.add(new Player(this, "Player2"));
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

	public List<Action> getAllActions() {
		List<Action> actions = new ArrayList<>();
		actions.addAll(getPlayers().stream().flatMap(player -> player.getActions().values().stream()).collect(Collectors.toList()));
		actions.addAll(getZones().stream().flatMap(zone -> zone.getCards().stream())
			.flatMap(card -> card.getActions().values().stream())
			.collect(Collectors.toList()));
		return actions;
		
	}
	
}
