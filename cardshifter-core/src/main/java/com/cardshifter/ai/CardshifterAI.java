package com.cardshifter.ai;

import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.Entity;

public interface CardshifterAI {
	ECSAction getAction(Entity player);
}
