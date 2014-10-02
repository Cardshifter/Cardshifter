package com.cardshifter.ai;

import net.zomis.aiscores.ScoreConfigFactory;
import net.zomis.aiscores.scorers.PredicateScorer;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;

public class AIs {

	public static ScoreConfigFactory<Entity, ECSAction> loser() {
		ScoreConfigFactory<Entity, ECSAction> config = new ScoreConfigFactory<>();
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(PhrancisGame.END_TURN_ACTION)));
		return config;
	}

}
