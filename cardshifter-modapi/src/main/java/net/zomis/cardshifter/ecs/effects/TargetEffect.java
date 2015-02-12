package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.Entity;

public interface TargetEffect {
	void perform(Entity source, Entity target);
}
