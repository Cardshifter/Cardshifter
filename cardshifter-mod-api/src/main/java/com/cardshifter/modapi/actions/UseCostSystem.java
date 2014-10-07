package com.cardshifter.modapi.actions;

import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class UseCostSystem extends SpecificActionSystem {

	private final ToIntFunction<Entity> cost;
	private final UnaryOperator<Entity> whoPays;
	private final ResourceRetriever useResource;

	public UseCostSystem(String action, ECSResource useResource, ToIntFunction<Entity> cost, UnaryOperator<Entity> whoPays) {
		super(action);
		this.useResource = ResourceRetriever.forResource(useResource);
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
		if (event.getEntity().isRemoved()) {
			return;
		}
		ECSResourceData have = useResource.resFor(whoPays.apply(event.getEntity()));
		int want = cost.applyAsInt(event.getEntity());
		have.change(-want);
	}

	@Override
	public String toString() {
		return "UseCostSystem [action=" + getActionName() + ", useResource=" + useResource + "]";
	}

}
