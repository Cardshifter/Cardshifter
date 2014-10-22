package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.PlayerComponent;

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
