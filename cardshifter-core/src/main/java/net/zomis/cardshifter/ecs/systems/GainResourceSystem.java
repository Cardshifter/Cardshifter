package net.zomis.cardshifter.ecs.systems;

import java.util.function.ToIntFunction;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.System;
import net.zomis.cardshifter.ecs.events.PhaseStartEvent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;

public class GainResourceSystem implements System {

	private ECSResource resource;
	private ToIntFunction<Entity> valueGet;

	public GainResourceSystem(ECSResource resource, ToIntFunction<Entity> object) {
		this.resource = resource;
		this.valueGet = object;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(PhaseStartEvent.class, turn -> {
			Entity entity = turn.getNewPhase().getOwner();
			ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
			int value = valueGet.applyAsInt(entity);
			map.getResource(resource).change(value);
		});
	}

}
