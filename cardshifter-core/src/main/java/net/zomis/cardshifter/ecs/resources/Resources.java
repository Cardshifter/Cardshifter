package net.zomis.cardshifter.ecs.resources;

import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.base.Entity;

public class Resources {

	public static void processResources(Entity card, Consumer<ECSResourceData> consumer) {
		card.getComponent(ECSResourceMap.class).getResources().forEach(consumer);
	}

}
