package com.cardshifter.modapi.phase;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.events.IEvent;

public abstract class PhaseChangeEvent implements IEvent {

	private final Phase oldPhase;
	private final Phase newPhase;
	private final PhaseController controller;
    private final ECSGame game;

	public PhaseChangeEvent(PhaseController controller, ECSGame game, Phase from, Phase to) {
		this.controller = controller;
        this.game = game;
		this.oldPhase = from;
		this.newPhase = to;
	}

	public Phase getNewPhase() {
		return newPhase;
	}
	
	public Phase getOldPhase() {
		return oldPhase;
	}
	
	public PhaseController getController() {
		return controller;
	}
	
    public ECSGame getGame() {
        return game;
    }
}
