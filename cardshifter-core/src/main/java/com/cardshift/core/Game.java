package com.cardshift.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.LuaValue;

public class Game {

	private final List<Zone> zones;
	private final LuaValue luaData;
	private final List<Player> players;
	private final Events events;
	
	public Game(File scriptDirectory) {
		this.zones = new ArrayList<>();
		this.luaData = LuaValue.tableOf();
		this.players = new ArrayList<>();
		this.events = new Events(scriptDirectory);
		
		this.players.add(new Player(this));
		this.players.add(new Player(this));
	}
	
	public LuaValue getLuaData() {
		return luaData;
	}
	
	public List<Zone> getZones() {
		return zones;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public Events getEvents() {
		return events;
	}
	
}
