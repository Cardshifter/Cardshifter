package com.cardshifter.console;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.phase.PhaseController;

import net.zomis.cardshifter.ecs.usage.CyborgChroniclesGame;

public class ConsoleControllerECS {
	private final ECSGame game;
	private final PhaseController phase;

	public ConsoleControllerECS(final ECSGame game) {
		this.game = Objects.requireNonNull(game, "game");;
		this.phase = ComponentRetriever.singleton(game, PhaseController.class);
	}

	public void play(Scanner input) {
		game.startGame();
		while (!game.isGameOver()) {
			outputGameState();
			final Entity performer = phase.getCurrentEntity();
			List<ECSAction> actions = Actions.getAllActions(game).stream().filter(action -> action.isAllowed(performer)).collect(Collectors.toList());
			outputList(actions);
			
			String in = input.nextLine();
			if (in.equals("exit")) {
				break;
			}
			
			handleActionInput(actions, in, input, performer);
		}
		print("--------------------------------------------");
		outputGameState();
		print("Game over!");
	}

	private void handleActionInput(final List<ECSAction> actions, final String in, Scanner input, Entity performer) {
		Objects.requireNonNull(actions, "actions");
		Objects.requireNonNull(in, "in");
		print("Choose an action:");
		
		try {
			int value = Integer.parseInt(in);
			if (value < 0 || value >= actions.size()) {
				print("Action index out of range: " + value);
				return;
			}
			
			ECSAction action = actions.get(value);
			print("Action " + action);
			if (action.isAllowed(performer)) {
				for (TargetSet actionOption : action.getTargetSets()) {
					List<Entity> targets = new ArrayList<>(actionOption.findPossibleTargets());
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
					Entity target = targets.get(targetIndex);
					actionOption.addTarget(target);
				}
				
				action.perform(performer);
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
//		final ComponentRetriever<ActionComponent> actions = ComponentRetriever.retreiverFor(ActionComponent.class);
		final PhaseController phases = game.getEntitiesWithComponent(PhaseController.class).iterator().next().getComponent(PhaseController.class);
		
		print("------------------");
		print(this.game);
		for (Entity player : game.getEntitiesWithComponent(PlayerComponent.class)) {
			print(player);
//			player.getActions().values().forEach(action -> print(4, "Action: " + action));
			printLua(4, player); // TODO: Some data should probably be hidden from other players, or even from self.
			
			for (ZoneComponent zone : player.getSuperComponents(ZoneComponent.class)) {
				if (zone.isKnownTo(phases.getCurrentPhase().getOwner())) {
					System.out.println("zone is known! " + zone);
					print(zone);
					zone.forEach(card -> {
						print(4, card);
//						card.getActions().values().forEach(action -> print(8, "Action: " + action));
						printLua(8, card);
					});
				}
			}
		}
	}

	private void print(final Object object) {
		print(0, object);
	}
	
	private void print(final int indentation, final Object object) {
		System.out.println(indent(indentation) + object.toString());
	}
	
	private void printLua(final int indentation, final Entity value) {
		for (Component comp : value.getSuperComponents(Component.class)) {
			print(indentation, comp);
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
		ECSMod mod = new CyborgChroniclesGame();
		ECSGame newgame = new ECSGame();
		mod.declareConfiguration(newgame);
		mod.setupGame(newgame);
		new ConsoleControllerECS(newgame).play(new Scanner(System.in, StandardCharsets.UTF_8.name()));
	}
}
