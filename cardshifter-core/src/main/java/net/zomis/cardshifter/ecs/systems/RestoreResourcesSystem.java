package net.zomis.cardshifter.ecs.systems;

import java.util.function.ToIntFunction;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.events.PhaseStartEvent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;

public class RestoreResourcesSystem implements System {

	private final ECSResource resource;
	private final ToIntFunction<Entity> valueGetter;

	public RestoreResourcesSystem(ECSResource resource, ToIntFunction<Entity> valueGetter) {
		this.resource = resource;
		this.valueGetter = valueGetter;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(PhaseStartEvent.class, turn -> {
			Entity entity = turn.getNewPhase().getOwner();
			ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
			int value = valueGetter.applyAsInt(entity);
			map.getResource(resource).set(value);
		});
	}

}
