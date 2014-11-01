package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retriever;

public class GameOverIfNoHealth implements ECSSystem {

	@Retriever
	private ComponentRetriever<PlayerComponent> player;
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
			if (event.getNewValue() <= 0 && player.has(event.getEntity())) {
				player.get(event.getEntity()).loseGame();
			}
		}
	}

}
