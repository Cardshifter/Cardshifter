package com.cardshifter.ai;

import net.zomis.aiscores.ScoreConfigFactory;
import net.zomis.aiscores.scorers.PredicateScorer;
import net.zomis.aiscores.scorers.SimpleScorer;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;

import com.cardshifter.ai.phrancis.AttackAnalyze;
import com.cardshifter.ai.phrancis.ScrapAnalyze;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.Entity;

public class AIs {
	
	public static ScoreConfigFactory<Entity, ECSAction> loser() {
		ScoreConfigFactory<Entity, ECSAction> config = new ScoreConfigFactory<>();
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(PhrancisGame.END_TURN_ACTION)));
		return config;
	}

	public static ScoreConfigFactory<Entity, ECSAction> idiot() {
		return new ScoreConfigFactory<>();
	}

	public static ScoreConfigFactory<Entity, ECSAction> medium() {
		ScoreConfigFactory<Entity, ECSAction> config = new ScoreConfigFactory<>();
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(PhrancisGame.PLAY_ACTION)), 10);
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(PhrancisGame.ENCHANT_ACTION)), -10); // this AI does not enchant
		config.withScorer(new SimpleScorer<>(ScrapAnalyze::scrapScore));
		config.withScorer(new SimpleScorer<>(AttackAnalyze::attackScore));
		return config;
	}

}
