package net.zomis.cardshifter.ecs.events;

import net.zomis.cardshifter.ecs.base.ECSGame;

public class StartGameEvent implements IEvent {
	
	private final ECSGame game;

	public StartGameEvent(ECSGame game) {
		this.game = game;
	}
	
	public ECSGame getGame() {
		return game;
	}

}
