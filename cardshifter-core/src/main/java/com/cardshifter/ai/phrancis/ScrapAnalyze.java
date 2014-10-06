package com.cardshifter.ai.phrancis;

import java.util.Comparator;
import java.util.List;

import net.zomis.aiscores.ScoreParameters;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.Retrievers;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.ZoneComponent;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

public class ScrapAnalyze {
	public static double scrapScore(ECSAction action, ScoreParameters<Entity> params) {
		if (!action.getName().equals(PhrancisGame.SCRAP_ACTION)) {
			return 0;
		}
		
		Entity entity = action.getOwner();
		ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
		ResourceRetriever attack = ResourceRetriever.forResource(PhrancisResources.ATTACK);
		ComponentRetriever<CardComponent> card = Retrievers.component(CardComponent.class);
		
		ZoneComponent battlefield = card.get(entity).getCurrentZone();
		
		List<Entity> creatures = battlefield.getCards();
		if (creatures.size() <= 3) {
			return -health.getFor(entity);
		}
		
		creatures.sort(Comparator.comparingInt(e -> health.getFor(e) + attack.getFor(e)));
		if (entity == creatures.get(0)) {
			// Only consider scrapping the creature with lowest health
			return 4 - health.getFor(entity);
		}
		return -1;
	}
}
