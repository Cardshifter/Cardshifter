package com.cardshifter.modapi.phase;

import java.util.function.ToIntFunction;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import net.zomis.cardshifter.ecs.effects.EntityInt;

public class GainResourceSystem implements ECSSystem {

	private ECSResource resource;
	private EntityInt valueGet;

	public GainResourceSystem(ECSResource resource, EntityInt object) {
		this.resource = resource;
		this.valueGet = object;
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, turn -> {
			Entity entity = turn.getNewPhase().getOwner();
			ECSResourceMap map = entity.getComponent(ECSResourceMap.class);
			int value = valueGet.valueFor(entity);
			map.getResource(resource).change(value);
		});
	}

	@Override
	public String toString() {
		return "GainResourceSystem [resource=" + resource + ", valueGet="
				+ valueGet + "]";
	}
	
}
