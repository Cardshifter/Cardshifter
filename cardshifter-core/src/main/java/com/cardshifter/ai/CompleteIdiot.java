package com.cardshifter.ai;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.actions.TargetSet;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;

public class CompleteIdiot implements CardshifterAI {

	private final Random random = new Random();
	
	private static Stream<ECSAction> getAllActions(ECSGame game) {
		return game.getEntitiesWithComponent(ActionComponent.class)
			.stream()
			.flatMap(entity -> entity.getComponent(ActionComponent.class)
					.getECSActions().stream());
	}
	
	@Override
	public ECSAction getAction(Entity player) {
		
		Stream<ECSAction> actions = getAllActions(player.getGame());
		
		Stream<ECSAction> allActions = actions.filter(action -> action.isAllowed(player))
				.filter(action -> setTargetIfPossible(action));
		List<ECSAction> list = allActions.collect(Collectors.toList());
		
		//return nothing if no actions are available
		if (list.isEmpty()) {
			return null;
		}

		//Do not scrap if it is the only thing you can do
		if (list.size() == 1) {
			for (ECSAction action : list) {
				if(action.getName().equals("Scrap")) {
					return null;
				}
			}
		}

		//parse the actions and return an appropriate one based on the actions available
		//For example, if there are less than 3 creatures on the board, do not scrap any
		//If any attacks are available, do those

		//return a random action from the list
		return list.get(random.nextInt(list.size()));
	}

	private boolean setTargetIfPossible(ECSAction action) {
		for (TargetSet targetset : action.getTargetSets()) {
			targetset.clearTargets();
			while (!targetset.hasEnoughTargets()) {
				List<Entity> targets = targetset.findPossibleTargets();
				if (targets.isEmpty()) {
					return false;
				}
				targetset.addTarget(targets.get(random.nextInt(targets.size())));
			}
			return true;
		}
		return true;
	}

}
