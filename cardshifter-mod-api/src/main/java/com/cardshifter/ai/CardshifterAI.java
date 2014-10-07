package com.cardshifter.ai;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.Entity;

public interface CardshifterAI {
	ECSAction getAction(Entity player);
}
