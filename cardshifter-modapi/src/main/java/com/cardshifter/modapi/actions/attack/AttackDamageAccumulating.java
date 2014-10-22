package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class AttackDamageAccumulating extends SpecificActionSystem {

	private final ResourceRetriever attack;
	private final ResourceRetriever health;

	public AttackDamageAccumulating(ECSResource attack, ECSResource health) {
		super("Attack");
		this.attack = ResourceRetriever.forResource(attack);
		this.health = ResourceRetriever.forResource(health);
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
		
		int attackDamage = attack.getFor(source);
		int defenseDamage = attack.getFor(target);
		
		damage(attackDamage, target);
		damage(defenseDamage, source);
	}

	private void damage(int damage, Entity target) {
		if (damage == 0) {
			return;
		}
		if (damage < 0) {
			throw new IllegalArgumentException("damage must be positive");
		}
		health.resFor(target).change(-damage);
		if (health.getFor(target) <= 0) {
			target.destroy();
		}
	}

}
