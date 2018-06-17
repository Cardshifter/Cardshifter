package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.base.*;

import java.util.function.IntPredicate;

public class GameOverIfTarget implements ECSSystem {

	@Retriever
	private ComponentRetriever<PlayerComponent> player;
	private final ECSResource resource;
	private final boolean win;
	private final IntPredicate test;

	public GameOverIfTarget(ECSResource resource, boolean win, IntPredicate test) {
		this.resource = resource;
		this.win = win;
		this.test = test;
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, this::endGame);
	}
	
	private void endGame(ResourceValueChange event) {
		if (event.getResource() == this.resource) {
			int value = event.getNewValue();

			if (test.test(value) && player.has(event.getEntity())) {
				PlayerComponent playerComponent = this.player.get(event.getEntity());
				playerComponent.eliminate(win);
			}
		}
	}

}
