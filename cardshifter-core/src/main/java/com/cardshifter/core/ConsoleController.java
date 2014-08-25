package com.cardshifter.core;

import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class ConsoleController {
	private final Game game;

	public ConsoleController(final Game game) {
		this.game = Objects.requireNonNull(game, "game");;
	}

	public void play() {
		Scanner input = new Scanner(System.in);
		while (true) {
			outputGameState();
			List<UsableAction> actions = game.getAllActions().stream().filter(action -> action.isAllowed()).collect(Collectors.toList());
			outputAvailableActions(actions);
			
			String in = input.nextLine();
			if (in.equals("exit")) {
				break;
			}
			
			handleActionInput(actions, in);
		}
	}

	private void handleActionInput(final List<UsableAction> actions, final String in) {
		Objects.requireNonNull(actions, "actions");
		Objects.requireNonNull(in, "in");
		print("Choose an action:");
		
		try {
			int value = Integer.parseInt(in);
			if (value < 0 || value >= actions.size()) {
				print("Action index out of range: " + value);
				return;
			}
			
			UsableAction action = actions.get(value);
			print("Action " + action + " on card " + action);
			if (action.isAllowed()) {
				action.perform();
				print("Action performed");
			}
			else {
				print("Action is not allowed");
			}
		}
		catch (NumberFormatException ex) {
			print("Illegal action index: " + in);
		}
	}

	private void outputAvailableActions(final List<UsableAction> actions) {
		Objects.requireNonNull(actions, "actions");
		print("------------------");
		ListIterator<UsableAction> it = actions.listIterator();
		while (it.hasNext()) {
			print(it.nextIndex() + ": " + it.next());
		}
	}

	private void outputGameState() {
		print("------------------");
		print(this.game);
		for (Player player : game.getPlayers()) {
			print(player);
			player.getActions().values().forEach(action -> print(4, "Action: " + action));
			printLua(4, player.data); // TODO: Some LuaData should probably be hidden from other players, or even from self.
		}
		
		for (Zone zone : game.getZones()) {
			print(zone);
			if (zone.isKnownToPlayer(game.getCurrentPlayer())) {
				zone.getCards().forEach(card -> {
					print(4, card);
					card.getActions().values().forEach(action -> print(8, "Action: " + action));
					printLua(8, card.data);
				});
			}
		}
	}

	private void print(final Object object) {
		print(0, object);
	}
	
	private void print(final int indentation, final Object object) {
		System.out.println(indent(indentation) + object.toString());
	}
	
	private void printLua(final LuaValue value) {
		printLua(0, value);
	}
	
	private void printLua(final int indentation, final LuaValue value) {
		processLuaTable(value.checktable(), (k, v) -> print(indentation, k + ": " + v));
	}
	
	private void processLuaTable(final LuaTable luaTable, final BiConsumer<LuaValue, LuaValue> pairConsumer) {
		luaTable.checktable();
		Objects.requireNonNull(pairConsumer, "pairConsumer");
		//search uses last key to find next key, starts with NIL
		LuaValue key = LuaValue.NIL;
		while (true) {
			Varargs pair = luaTable.next(key);
			key = pair.arg1();
			if (key.isnil()) {
				//no more keys
				break;
			}
			pairConsumer.accept(key, pair.arg(2));
		}
	}
	
	private String indent(final int amount) {
		if (amount == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(amount);
		for (int i = 0; i < amount; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		InputStream file = Game.class.getResourceAsStream("start.lua");
		Game game = new Game(file);
		game.getEvents().startGame(game);
		new ConsoleController(game).play();		
	}
}
