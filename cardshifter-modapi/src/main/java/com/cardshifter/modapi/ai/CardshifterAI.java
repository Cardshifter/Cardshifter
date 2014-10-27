package com.cardshifter.modapi.ai;

import net.zomis.cardshifter.ecs.config.ConfigComponent;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.Entity;

public interface CardshifterAI {
	ECSAction getAction(Entity player);
	
	default void configure(Entity entity, ConfigComponent config) {
		
	}
}
