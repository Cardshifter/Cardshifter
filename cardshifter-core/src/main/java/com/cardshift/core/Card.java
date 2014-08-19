package com.cardshift.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.luaj.vm2.LuaValue;

public class Card {

	private Zone zone;
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
	
	public void destroy() {
		zoneMoveInternal(null, false);
	}
	
	/**
	 * Move this card to the top of another zone
	 * 
	 * @param destination Zone to move to
	 */
	public void moveToTopOf(Zone destination) {
		zoneMoveInternal(destination, true);
	}
	
	/**
	 * Move this card to the bottom of another zone
	 * 
	 * @param destination Zone to move to
	 */
	public void moveToBottomOf(Zone destination) {
		zoneMoveInternal(destination, false);
	}
	
	private void zoneMoveInternal(Zone destination, boolean top) {
		Zone zone = this.getZone();
		Game game = zone.getGame();
		Objects.requireNonNull(game);
		
		destination = game.getEvents().zoneMove(this, zone, destination);
		zone.getCards().remove(this);
		
		if (destination != null) {
			if (top) {
				destination.getCards().addFirst(this);
			}
			else {
				destination.getCards().addLast(this);
			}
		}
		this.zone = destination;
	}
	
}
