package com.cardshifter.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import com.cardshifter.core.actions.PlayerAction;
import com.cardshifter.core.actions.UsableAction;

@Deprecated
public class Player implements Targetable, IdEntity {

	public final LuaTable data = new ExtLuaTable(this::onChange);
	
	private final Game game;
	private final String name;

	private final Map<String, UsableAction> actions = new HashMap<>();
	private final int id;
	
	public Player(Game game, String name, int id) {
		this.game = Objects.requireNonNull(game, "game");
		this.name = Objects.requireNonNull(name, "name");;
		this.id = id;
	}
	
	private void onChange(Object key, Object value) {
		System.out.println(this + ": " + key + " = " + value);
		getGame().broadcastChange(this, key, value);
	}
	
	public Player getNextPlayer() {
		List<Player> players = game.getPlayers();
		int index = players.indexOf(this);
		if (index == players.size() - 1) {
			return players.get(0);
		}
		return players.get(index + 1);
	}
	
	/**
	 * Returns a list of all opponents of this player, ordered by who is the next opponent
	 * 
	 * @return Ordered list of opponents, ordered by player order
	 */
	public List<Player> getOpponents() {
		List<Player> players = game.getPlayers();
		int index = players.indexOf(this);
		
		List<Player> before = players.subList(0, index);
		List<Player> after = players.subList(index + 1, players.size());

		after.addAll(before);
		return after;
	}
	
	public Game getGame() {
		return game;
	}
	
	public Map<String, UsableAction> getActions() {
		return actions;
	}
	
	@Override
	public String toString() {
		return "{Player '" + this.name + "'}";
	}
	
	public PlayerAction addAction(final String name, final LuaValue actionAllowed, final LuaValue actionPerformed) {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(actionAllowed, "actionAllowed");
		Objects.requireNonNull(actionPerformed, "actionPerformed");
		PlayerAction action = new PlayerAction(this, name, actionAllowed, actionPerformed);
		actions.put(name, action);
		return action;
	}

	@Override
	public LuaTable getData() {
		return data;
	}

	public int getIndex() {
		return getGame().getPlayers().indexOf(this);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int getId() {
		return id;
	}
}
