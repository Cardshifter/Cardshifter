package com.cardshifter.ai.phrancis;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.resources.ResourceRetriever;

import net.zomis.aiscores.ScoreParameters;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

public class AttackAnalyze {

	private static final ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
	private static final ResourceRetriever attack = ResourceRetriever.forResource(PhrancisResources.ATTACK);
	
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
}
