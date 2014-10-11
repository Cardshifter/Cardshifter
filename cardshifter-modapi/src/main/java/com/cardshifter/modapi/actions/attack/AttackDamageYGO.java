package com.cardshifter.modapi.actions.attack;

import java.util.function.Predicate;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class AttackDamageYGO extends SpecificActionSystem {

	private final ResourceRetriever attack;
	private final ResourceRetriever health;
	private final Predicate<Entity> trample;
	
	public AttackDamageYGO(ECSResource attack, ECSResource health, Predicate<Entity> hasTrample) {
		super("Attack");
		this.attack = ResourceRetriever.forResource(attack);
		this.health = ResourceRetriever.forResource(health);
		this.trample = hasTrample;
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		Entity source = event.getEntity();
		Entity target = event.getAction().getTargetSets().get(0).getChosenTargets().get(0);
		
		int attackDamage = attack.getFor(source);
		int defenseDamage = attack.getFor(target);
		
		if (target.hasComponent(PlayerComponent.class)) {
			damage(attackDamage, target);
			destroyOrNothing(defenseDamage, source);
		}
		else {
			Entity player = target.getComponent(CardComponent.class).getOwner();
			int overflowDamage = destroyOrNothing(attackDamage, target);
			if (overflowDamage > 0 && trample.test(source)) {
				damage(overflowDamage, player);
			}
			destroyOrNothing(defenseDamage, source);
		}
	}

	private void damage(int damage, Entity target) {
		if (damage <= 0) {
			throw new IllegalArgumentException("damage must be positive");
		}
		health.resFor(target).change(-damage);
	}

	private int destroyOrNothing(int damage, Entity target) {
		int overflowDamage = damage - health.getFor(target);
		if (overflowDamage >= 0) {
			target.destroy();
		}
		return overflowDamage;
	}

}
