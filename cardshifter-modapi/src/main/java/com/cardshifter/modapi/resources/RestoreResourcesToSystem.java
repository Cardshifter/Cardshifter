package com.cardshifter.modapi.resources;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.phase.PhaseStartEvent;

public class RestoreResourcesToSystem implements ECSSystem {

	private final Predicate<Entity> entityPredicate;
	private final ResourceRetriever resource;
	private final ToIntFunction<Entity> valueGetter;

	public RestoreResourcesToSystem(Predicate<Entity> entities, ECSResource resource, ToIntFunction<Entity> valueGetter) {
		this.entityPredicate = entities;
		this.resource = ResourceRetriever.forResource(resource);
		this.valueGetter = valueGetter;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, this::restoreResources);
	}
	
	private void restoreResources(PhaseStartEvent event) {
		ECSGame game = event.getNewPhase().getOwner().getGame();
		List<Entity> entities = game.findEntities(entityPredicate);
		entities.forEach(this::restoreResource);
	}
	
	private void restoreResource(Entity entity) {
		int value = valueGetter.applyAsInt(entity);
		resource.resFor(entity).set(value);
	}

	@Override
	public String toString() {
		return "RestoreResourcesToSystem [entityPredicate=" + entityPredicate
				+ ", resource=" + resource + ", valueGetter=" + valueGetter
				+ "]";
	}

}
