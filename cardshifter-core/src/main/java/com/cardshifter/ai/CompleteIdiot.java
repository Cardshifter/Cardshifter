package com.cardshifter.ai;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cardshifter.core.Player;
import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;

public class CompleteIdiot implements CardshifterAI {

	private final Random random = new Random();
	
	@Override
	public UsableAction getAction(Player player) {
		Stream<UsableAction> actions = player.getActions().values().stream();
		Stream<UsableAction> cardActions = player.getGame().getZones().stream()
			.flatMap(zone -> zone.getCards().stream())
			.flatMap(card -> card.getActions().values().stream());
		
		Stream<UsableAction> allActions = Stream.concat(actions, cardActions).filter(action -> action.isAllowed())
				.filter(action -> setTargetIfPossible(action));
		List<UsableAction> list = allActions.collect(Collectors.toList());
		
		if (list.isEmpty()) {
			return null;
		}
		return list.get(random.nextInt(list.size()));
	}

	private boolean setTargetIfPossible(UsableAction action) {
		if (action instanceof TargetAction) {
			TargetAction targetAction = (TargetAction) action;
			List<Targetable> targets = targetAction.findTargets();
			if (targets.isEmpty()) {
				return false;
			}
			targetAction.setTarget(targets.get(random.nextInt(targets.size())));
			return true;
		}
		return true;
	}

}
