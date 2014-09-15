package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.resources.ResourceValueChange;

public class GameOverIfNoHealth implements System {

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(ResourceValueChange.class, event -> game.endGame());
	}

}
