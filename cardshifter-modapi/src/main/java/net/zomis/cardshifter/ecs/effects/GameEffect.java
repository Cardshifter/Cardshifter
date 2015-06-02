package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.base.Entity;

@FunctionalInterface
public interface GameEffect {
	void accept(Entity entity, ActionPerformEvent extraData);

    default GameEffect andThen(GameEffect effect) {
        return (l, r) -> {
            accept(l, r);
            effect.accept(l, r);
        };
    }
}
