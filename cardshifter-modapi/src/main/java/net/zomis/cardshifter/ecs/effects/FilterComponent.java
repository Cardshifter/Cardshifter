package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.CopyableComponent;
import com.cardshifter.modapi.base.Entity;

public class FilterComponent extends Component implements CopyableComponent {

	private final TargetFilter filter;
	private final int minTargetCount;
	private final int maxTargetCount;

	public FilterComponent(TargetFilter filter, int minTargetCount, int maxTargetCount) {
		this.filter = filter;
		this.minTargetCount = minTargetCount;
		this.maxTargetCount = maxTargetCount;
	}
	
	@Override
	public Component copy(Entity copyTo) {
		return new FilterComponent(filter, minTargetCount, maxTargetCount);
	}
	
	public boolean check(TargetableCheckEvent event) {
		return filter.test(event.getAction().getOwner(), event.getTarget());
	}

	public int getMinTargetCount() {
		return minTargetCount;
	}

	public int getMaxTargetCount() {
		return maxTargetCount;
	}

}
