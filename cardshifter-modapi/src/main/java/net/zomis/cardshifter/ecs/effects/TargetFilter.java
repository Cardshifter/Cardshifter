package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.Entity;

import java.util.Arrays;

public interface TargetFilter {
	boolean test(Entity source, Entity target);
	
	default TargetFilter and(TargetFilter other) {
		return (source, target) -> this.test(source, target) && other.test(source, target);
	}

    public static TargetFilter or(TargetFilter... alternatives) {
        return (source, target) -> Arrays.stream(alternatives).anyMatch(p -> p.test(source, target));
    }

}
