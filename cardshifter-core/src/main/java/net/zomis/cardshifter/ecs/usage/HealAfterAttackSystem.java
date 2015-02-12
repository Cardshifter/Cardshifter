package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class HealAfterAttackSystem extends SpecificActionSystem {

	private final Entity entity;
	private final ResourceRetriever health;
	private final ResourceRetriever healthMax;

	public HealAfterAttackSystem(Entity entity, ECSResource health, ECSResource healthMax) {
		super("Attack");
		this.entity = entity;
		this.health = ResourceRetriever.forResource(health);
		this.healthMax = ResourceRetriever.forResource(healthMax);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		if (event.getEntity() != entity) {
			return;
		}
		if (entity.isRemoved()) {
			return;
		}
		health.resFor(entity).set(healthMax.getFor(entity));
	}
	
	

}
