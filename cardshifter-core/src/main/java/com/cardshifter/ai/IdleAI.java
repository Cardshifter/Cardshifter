package com.cardshifter.ai;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.Entity;

/**
 * An AI that does absolutely nothing. Refuses to play.
 * @author Simon Forsberg
 *
 */
public class IdleAI implements CardshifterAI {

	@Override
	public ECSAction getAction(Entity player) {
		return null;
	}

}
