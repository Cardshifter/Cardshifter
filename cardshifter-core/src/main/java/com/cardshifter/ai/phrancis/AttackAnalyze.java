package com.cardshifter.ai.phrancis;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.resources.ResourceRetriever;

import net.zomis.aiscores.ScoreParameters;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

public class AttackAnalyze {

	private static final ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
	private static final ResourceRetriever attack = ResourceRetriever.forResource(PhrancisResources.ATTACK);
	private static final ResourceRetriever scrapCost = ResourceRetriever.forResource(PhrancisResources.SCRAP_COST);
	
	public static double attackScore(ECSAction action, ScoreParameters<Entity> params) {
		if (!action.getName().equals(PhrancisGame.ATTACK_ACTION)) {
			return 0;
		}
		
		TargetSet targets = action.getTargetSets().get(0);
		targets.clearTargets();
		List<Entity> possibleTargets = targets.findPossibleTargets();
		Optional<Entity> player = possibleTargets.stream().filter(e -> e.hasComponent(PlayerComponent.class)).findAny();
		if (player.isPresent()) {
			// attacking a player is really the best option there is
			targets.addTarget(player.get());
			return 100;
		}
		if (possibleTargets.isEmpty()) {
			throw new RuntimeException("Attack action has no targets: " + action);
		}
		
		possibleTargets.sort(Comparator.comparingInt(e -> attackVS(action.getOwner(), e)));
		Entity chosenTarget = possibleTargets.get(possibleTargets.size() - 1);
		targets.addTarget(chosenTarget);

		return attackVS(action.getOwner(), chosenTarget);
	}

	private static int attackVS(Entity attacker, Entity target) {
		int score = 0;
		boolean attackerDies = attack.getFor(target) >= health.getFor(attacker);
		boolean targetDies = attack.getFor(attacker) >= health.getFor(target);
		int trampleDamage = attack.getFor(attacker) - health.getFor(target);
		int additionalDamageBack = attack.getFor(target) - health.getFor(attacker);
		if (attackerDies && !targetDies) {
			// this is really the worst option there is
			return -10;
		}
		
		if (targetDies) {
			score += 100;
			score -= trampleDamage;
		}
		if (attackerDies) {
			score -= 97;
			score += additionalDamageBack;
		}
		return score;
	}
	
	public static double scrapNeeded(ECSAction action, ScoreParameters<Entity> params) {
		if (!action.getName().equals(PhrancisGame.SCRAP_ACTION)) {
			return 0;
		}
		
		Entity entity = action.getOwner();
		ComponentRetriever<CardComponent> card = Retrievers.component(CardComponent.class);
		Entity owner = card.get(entity).getOwner();
		HandComponent hand = owner.getComponent(HandComponent.class);
		return hand.stream().mapToInt(e -> scrapCost.getOrDefault(e, 0)).sum();
	}
	
	public static double scrapIfCanGetKilled(ECSAction action, ScoreParameters<Entity> params) {
		if (!action.getName().equals(PhrancisGame.SCRAP_ACTION)) {
			return 0;
		}
		
		Entity entity = action.getOwner();
		ComponentRetriever<CardComponent> card = Retrievers.component(CardComponent.class);
		Entity owner = card.get(entity).getOwner();
		Set<Entity> players = entity.getGame().getEntitiesWithComponent(PlayerComponent.class);
		Entity opponent = players.stream().filter(pl -> pl.getComponent(PlayerComponent.class).getIndex() != owner.getComponent(PlayerComponent.class).getIndex()).findAny().get();
		
		ZoneComponent battlefield = opponent.getComponent(BattlefieldComponent.class);
		int myHealth = health.getFor(entity);
		int myAttack = attack.getFor(entity);
		
		List<Entity> creaturesCanKill = battlefield.stream().filter(e -> attack.getOrDefault(e, 0) >= myHealth).collect(Collectors.toList());
		if (creaturesCanKill.isEmpty()) {
			// This creature cannot die from any opponent creature, then it is safe
			return -10;
		}
		
		Stream<Entity> creaturesCanNotDie = creaturesCanKill.stream().filter(e -> health.getOrDefault(e, 0) > myAttack);
		if (creaturesCanNotDie.findAny().isPresent()) {
			// If I stay here, then I am toast.
			return 1;
		}
		
		// We can kill each other
		return 0.25;
	}
	
	public static double scrapScore(ECSAction action, ScoreParameters<Entity> params) {
		if (!action.getName().equals(PhrancisGame.SCRAP_ACTION)) {
			return 0;
		}
		
		Entity entity = action.getOwner();
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
	
	public static double health(ECSAction action, ScoreParameters<Entity> params) {
		return health.getOrDefault(action.getOwner(), 0);
	}
	
	public static double attack(ECSAction action, ScoreParameters<Entity> params) {
		return attack.getOrDefault(action.getOwner(), 0);
	}
	
	public static double enchantScore(ECSAction action, ScoreParameters<Entity> params) {
		if (!action.getName().equals(PhrancisGame.ENCHANT_ACTION)) {
			return 0;
		}
		
		TargetSet targets = action.getTargetSets().get(0);
		targets.clearTargets();
		List<Entity> possibleTargets = targets.findPossibleTargets();
		
		if (possibleTargets.isEmpty()) {
			return -1;
		}
		
		Entity enchantment = action.getOwner();
		int attackBonus = attack.getFor(enchantment);
		int healthBonus = health.getFor(enchantment);
		
		possibleTargets.sort(Comparator.comparingDouble(e -> enchantScore(e, attackBonus, healthBonus)));
		Entity chosenTarget = possibleTargets.get(possibleTargets.size() - 1);
		targets.addTarget(chosenTarget);

		return enchantScore(chosenTarget, attackBonus, healthBonus);
	}

	private static double enchantScore(Entity e, int attackBonus, int healthBonus) {
		return 1.5*(health.getFor(e) + healthBonus) + attack.getFor(e) + attackBonus;
	}

}
