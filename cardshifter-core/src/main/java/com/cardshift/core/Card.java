package com.cardshift.core;

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.LuaValue;

public class Card {

	private final Zone zone;
	private final Map<String, Action> actions;
	public final LuaValue data;
	
	Card(Zone zone) {
		this.data = LuaValue.tableOf();
		this.zone = zone;
		this.actions = new HashMap<>();
	}
	
	public Zone getZone() {
		return zone;
	}
	
	public Action addAction(Action action) {
		this.actions.put(action.getName(), action);
		return action;
	}
	
	public Action addAction(String name, LuaValue actionAllowed, LuaValue actionPerformed) {
		Action action = new Action(name, actionAllowed, actionPerformed);
		actions.put(name, action);
		return action;
	}
	
	public Player getOwner() {
		if (zone == null) {
			throw new NullPointerException("Card is not inside a zone: " + this);
		}
		return zone.getOwner();
	}
	
	public Map<String, Action> getActions() {
		return actions;
	}
	
	public Action getAction(String name) {
		return actions.get(name);
	}
	
}
