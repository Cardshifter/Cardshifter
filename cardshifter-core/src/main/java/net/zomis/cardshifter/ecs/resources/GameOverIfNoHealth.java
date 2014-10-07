package net.zomis.cardshifter.ecs.resources;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.components.PlayerComponent;

public class GameOverIfNoHealth implements ECSSystem {

	private final ECSResource resource;

	public GameOverIfNoHealth(ECSResource resource) {
		this.resource = resource;
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, this::endGame);
	}
	
	private void endGame(ResourceValueChange event) {
		if (event.getResource() == this.resource) {
			if (event.getNewValue() <= 0 && event.getEntity().hasComponent(PlayerComponent.class)) {
				event.getEntity().getGame().endGame();
			}
		}
	}

}
