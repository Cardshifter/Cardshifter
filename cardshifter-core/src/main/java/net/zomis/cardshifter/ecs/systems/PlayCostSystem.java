package net.zomis.cardshifter.ecs.systems;

import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.events.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.events.ActionPerformEvent;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceData;
import net.zomis.cardshifter.ecs.resources.ResourceRetreiver;

public class PlayCostSystem extends SpecificActionSystem {

	private final ToIntFunction<Entity> cost;
	private final UnaryOperator<Entity> whoPays;
	private final ResourceRetreiver useResource;

	public PlayCostSystem(String action, ECSResource useResource, ToIntFunction<Entity> cost, UnaryOperator<Entity> whoPays) {
		super(action);
		this.useResource = ResourceRetreiver.forResource(useResource);
		this.cost = cost;
		this.whoPays = whoPays;
	}

	@Override
	protected void isAllowed(ActionAllowedCheckEvent event) {
		ECSResourceData have = useResource.resFor(whoPays.apply(event.getEntity()));
		int want = cost.applyAsInt(event.getEntity());
		if (!have.has(want)) {
			event.setAllowed(false);
		}
	}
	
	@Override
	protected void onPerform(ActionPerformEvent event) {
		ECSResourceData have = useResource.resFor(whoPays.apply(event.getEntity()));
		int want = cost.applyAsInt(event.getEntity());
		have.change(-want);
	}

}
