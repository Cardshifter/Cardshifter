package com.cardshifter.core;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class ConsoleController {

	private final Game game;

	public ConsoleController(Game game) {
		this.game = game;
	}

	public static void main(String[] args) {
		InputStream file = Game.class.getResourceAsStream("start.lua");
		Game game = new Game(file);
		game.getEvents().startGame(game);
		new ConsoleController(game).play();		
		
	}

	public void play() {
		Scanner input = new Scanner(System.in);
		while (true) {
			outputGameState();
			List<Action> actions = game.getAllActions();
			outputAvailableActions(actions);
			
			String in = input.nextLine();
			if (in.equals("exit")) {
				break;
			}
			
			handleActionInput(actions, in);
		}
		
		input.close();
	}

	private void handleActionInput(List<Action> actions, String in) {
		print("Choose an action:");
		
		try {
			Integer value = Integer.parseInt(in);
			if (value < 0 || value >= actions.size()) {
				print("Action index out of range: " + value);
				return;
			}
			
			Action action = actions.get(value);
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

	private void outputAvailableActions(List<Action> actions) {
		print("------------------");
		int i = 0;
		Iterator<Action> it = actions.iterator();
		while (it.hasNext()) {
			Action action = it.next();
			System.out.println(i + ": " + action);
			i++;
		}
	}

	private void outputGameState() {
		print("------------------");
		print(this.game);
		for (Player player : game.getPlayers()) {
			print(player);
			player.getActions().values().forEach(action -> System.out.println("    Action: " + action));
			printLua("    ", player.data);
		}
		
		for (Zone zone : game.getZones()) {
			print(zone);
			zone.getCards().forEach(card -> {
				print("    " + card);
				card.getActions().values().forEach(action -> System.out.println("        Action: " + action));
				printLua("        ", card.data);
			});
		}
	}

	private void print(Object string) {
		System.out.println(string);
	}
	
	private void printLua(String prefix, LuaValue value) {
		LuaValue k = LuaValue.NIL;
		while (true) {
			Varargs n = value.next(k);
			if ( (k = n.arg1()).isnil() )
				break;
			LuaValue v = n.arg(2);
			print(prefix + k + ": " + v);
		}
	}
	
}
