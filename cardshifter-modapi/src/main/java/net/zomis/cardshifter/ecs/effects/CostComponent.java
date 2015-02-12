package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.resources.ECSResource;

public class CostComponent extends Component {

	private final ECSResource resource;
	private final int cost;

	public CostComponent(ECSResource resource, int cost) {
		this.resource = resource;
		this.cost = cost;
	}
	
	public int getCost() {
		return cost;
	}
	
	public ECSResource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return "CostComponent [resource=" + resource + ", cost=" + cost + "]";
	}
	
}
