package net.zomis.cardshifter.ecs.resources;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.phase.PhaseStartEvent;

public class RestoreResourcesToSystem implements ECSSystem {

	private final Predicate<Entity> entityPredicate;
	private final ResourceRetreiver resource;
	private final ToIntFunction<Entity> valueGetter;

	public RestoreResourcesToSystem(Predicate<Entity> entities, ECSResource resource, ToIntFunction<Entity> valueGetter) {
		this.entityPredicate = entities;
		this.resource = ResourceRetreiver.forResource(resource);
		this.valueGetter = valueGetter;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(PhaseStartEvent.class, this::restoreResources);
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

}
