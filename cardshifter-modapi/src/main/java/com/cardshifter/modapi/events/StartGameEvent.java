package com.cardshifter.modapi.events;

import com.cardshifter.modapi.base.ECSGame;

public class StartGameEvent implements IEvent {
	
	private final ECSGame game;

	public StartGameEvent(ECSGame game) {
		this.game = game;
	}
	
	public ECSGame getGame() {
		return game;
	}

}
