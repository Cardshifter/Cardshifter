package com.cardshifter.core.console;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.luaj.vm2.LuaValue;

import com.cardshifter.core.Game;
import com.cardshifter.core.LuaTools;
import com.cardshifter.core.Player;
import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import com.cardshifter.core.Zone;

public class ConsoleController {
	private final Game game;

	public ConsoleController(final Game game) {
		this.game = Objects.requireNonNull(game, "game");;
	}

	public void play(Scanner input) {
		while (!game.isGameOver()) {
			outputGameState();
			List<UsableAction> actions = game.getAllActions().stream().filter(action -> action.isAllowed()).collect(Collectors.toList());
			outputList(actions);
			
			String in = input.nextLine();
			if (in.equals("exit")) {
				break;
			}
			
			handleActionInput(actions, in, input);
		}
		print("--------------------------------------------");
		outputGameState();
		print("Game over!");
	}

	private void handleActionInput(final List<UsableAction> actions, final String in, Scanner input) {
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
			print("Action " + action);
			if (action.isAllowed()) {
				if (action instanceof TargetAction) {
					TargetAction targetAction = (TargetAction) action;
					List<Targetable> targets = targetAction.findTargets();
					if (targets.isEmpty()) {
						print("No available targets for action");
						return;
					}
					
					outputList(targets);
					print("Enter target index:");
					int targetIndex = Integer.parseInt(input.nextLine());
					if (value < 0 || value >= actions.size()) {
						print("Target index out of range: " + value);
						return;
					}
					
					Targetable target = targets.get(targetIndex);
					targetAction.perform(target);
				}
				else action.perform();
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

	private void outputList(final List<?> actions) {
		Objects.requireNonNull(actions, "actions");
		print("------------------");
		ListIterator<?> it = actions.listIterator();
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
	
	private void printLua(final int indentation, final LuaValue value) {
		LuaTools.processLuaTable(value.checktable(), (k, v) -> print(indentation, k + ": " + v));
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
}
