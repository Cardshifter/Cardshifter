package com.cardshifter.ai;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cardshifter.core.Player;
import com.cardshifter.core.UsableAction;

public class CompleteIdiot implements CardshifterAI {

	private final Random random = new Random();
	
	@Override
	public UsableAction getAction(Player player) {
		Stream<UsableAction> actions = player.getActions().values().stream();
		Stream<UsableAction> cardActions = player.getGame().getZones().stream()
			.flatMap(zone -> zone.getCards().stream())
			.flatMap(card -> card.getActions().values().stream());
		
		Stream<UsableAction> allActions = Stream.concat(actions, cardActions).filter(action -> action.isAllowed());
		List<UsableAction> list = allActions.collect(Collectors.toList());
		
		// TODO: If it is a TargetAction, make sure that there are valid targets for it.
		
		if (list.isEmpty()) {
			return null;
		}
		return list.get(random.nextInt(list.size()));
	}

}
