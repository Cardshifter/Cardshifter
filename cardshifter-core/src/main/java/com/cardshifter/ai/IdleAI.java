package com.cardshifter.ai;

import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.Entity;

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
