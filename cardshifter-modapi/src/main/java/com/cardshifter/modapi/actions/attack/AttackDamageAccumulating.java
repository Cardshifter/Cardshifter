package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

import java.util.function.BiPredicate;

public class AttackDamageAccumulating extends SpecificActionSystem {

	private final ResourceRetriever attack;
	private final ResourceRetriever health;
	private final BiPredicate<Entity, Entity> allowCounterAttack;

	public AttackDamageAccumulating(ECSResource attack, ECSResource health,
		BiPredicate<Entity, Entity> allowCounterAttack) {
		super("Attack");
		this.attack = ResourceRetriever.forResource(attack);
		this.health = ResourceRetriever.forResource(health);
		this.allowCounterAttack = allowCounterAttack;
	}

	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		if (attack.getFor(event.getEntity()) <= 0) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
		
		Entity source = event.getEntity();
		Entity target = event.getAction().getTargetSets().get(0).getChosenTargets().get(0);
		
		AttackEvent attackEvent = new AttackEvent(source, target);
		source.getGame().executeCancellableEvent(attackEvent, () -> {
			int attackDamage = attack.getFor(source);
			int defenseDamage = attack.getFor(target);
			ECSGame game = source.getGame();
			damage(attackDamage, target, source, game);
			if (allowCounterAttack.test(source, target)) {
				damage(defenseDamage, source, target, game);
			}

			checkKill(target);
			checkKill(source);
		});
	}

	private void checkKill(Entity target) {
		if (health.getFor(target) <= 0 && !target.hasComponent(PlayerComponent.class)) {
			target.destroy();
		}
	}

	private void damage(int damage, Entity target, Entity damagedBy, ECSGame game) {
		if (damage == 0) {
			return;
		}
		if (damage < 0) {
			throw new IllegalArgumentException("damage must be positive");
		}
		game.getEvents().executeEvent(new DamageEvent(target, damagedBy, damage), e -> health.resFor(target).change(-e.getDamage()));
	}

}
