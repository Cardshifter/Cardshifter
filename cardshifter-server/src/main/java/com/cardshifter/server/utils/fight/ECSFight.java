package com.cardshifter.server.utils.fight;

import com.cardshifter.modapi.base.ECSGame;

import net.zomis.fight.ext.GameFightNew;

public class ECSFight<A> {

	private GameFightNew<A, ECSGame> fight;

	public ECSFight() {
		this.fight = new GameFightNew<>();
	}
	
}
