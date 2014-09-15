package net.zomis.cardshifter.ecs.systems;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceValueChange;

public class GameOverIfNoHealth implements System {

	private final ECSResource resource;

	public GameOverIfNoHealth(ECSResource resource) {
		this.resource = resource;
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(ResourceValueChange.class, this::endGame);
	}
	
	private void endGame(ResourceValueChange event) {
		if (event.getResource() == this.resource) {
			if (event.getNewValue() <= 0 && event.getEntity().hasComponent(PlayerComponent.class)) {
				event.getEntity().getGame().endGame();
			}
		}
	}

}
