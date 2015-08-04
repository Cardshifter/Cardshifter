package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.events.IEvent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

import java.util.function.BiPredicate;

public class AttackDamageYGO extends SpecificActionSystem {

	private final ResourceRetriever attack;
	private final ResourceRetriever health;
	private final BiPredicate<Entity, Entity> allowCounterAttack;

	public AttackDamageYGO(ECSResource attack, ECSResource health, BiPredicate<Entity, Entity> allowCounterAttack) {
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
		ECSGame game = event.getEntity().getGame();
		
		int attackDamage = attack.getFor(source);
		int defenseDamage = attack.getFor(target);
		
		if (target.hasComponent(PlayerComponent.class)) {
			damage(event, attackDamage, target, source, game);
		}
		else {
			DamageEvent damageEvent = new DamageEvent(event, target, source, attackDamage);
			game.getEvents().executeEvent(damageEvent, e -> {
			});
			destroyOrNothing(damageEvent.getDamage(), target);
		}

		if (allowCounterAttack.test(source, target)) {
			destroyOrNothing(defenseDamage, source);
		}
	}

	private void damage(IEvent cause, int damage, Entity target, Entity damagedBy, ECSGame game) {
		if (damage <= 0) {
			return;
		}
		game.getEvents().executeEvent(new DamageEvent(cause, target, damagedBy, damage),
                e -> health.resFor(target).change(-e.getDamage()));
	}

	private int destroyOrNothing(int damage, Entity target) {
		int overflowDamage = damage - health.getFor(target);
		if (overflowDamage >= 0) {
			target.destroy();
		}
		return overflowDamage;
	}

}
