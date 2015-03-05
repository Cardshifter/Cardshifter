package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.CopyableComponent;
import com.cardshifter.modapi.base.Entity;

public class FilterComponent extends Component implements CopyableComponent {

	private final TargetFilter filter;
	
	public FilterComponent(TargetFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public Component copy(Entity copyTo) {
		return new FilterComponent(filter);
	}
	
	public boolean check(TargetableCheckEvent event) {
		return filter.test(event.getAction().getOwner(), event.getTarget());
	}

}
