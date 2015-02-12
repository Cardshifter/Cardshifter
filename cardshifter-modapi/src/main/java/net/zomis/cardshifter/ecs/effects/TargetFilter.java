package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.Entity;

public interface TargetFilter {
	boolean isTargetable(Entity source, Entity target);
	
	default TargetFilter and(TargetFilter other) {
		return (source, target) -> this.isTargetable(source, target) && other.isTargetable(source, target);
	}
}
